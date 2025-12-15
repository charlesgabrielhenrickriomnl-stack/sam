package auth.proj.sam.controller;

import auth.proj.sam.dto.ChangePasswordDto;
import auth.proj.sam.dto.VerifyPasswordChangeDto;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.UserRepository;
import auth.proj.sam.service.MfaService;
import auth.proj.sam.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Controller
public class SettingsController {

    private final UserRepository userRepository;
    private final MfaService mfaService;
    private final UserService userService;

    public SettingsController(UserRepository userRepository, MfaService mfaService, UserService userService) {
        this.userRepository = userRepository;
        this.mfaService = mfaService;
        this.userService = userService;
    }

    @GetMapping("/settings")
    public String showSettingsPage(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        
        // Add DTOs for the forms
        if (!model.containsAttribute("changePasswordDto")) {
             model.addAttribute("changePasswordDto", new ChangePasswordDto());
        }
        // NEW: Add DTO for the second step
        if (!model.containsAttribute("verifyPasswordChangeDto")) {
             model.addAttribute("verifyPasswordChangeDto", new VerifyPasswordChangeDto());
        }

        // NEW: Check for ROLE_DEPARTMENT and serve a simplified settings page
        boolean isDepartment = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT"));
        
        if (isDepartment) {
            return "department-settings"; 
        }

        // --- Existing Student/Teacher Logic Below ---

        // Profile-related logic from ProfileController
        if (user.getLastDisplayNameChange() != null) {
            long daysSinceLastChange = ChronoUnit.DAYS.between(user.getLastDisplayNameChange(), LocalDateTime.now());
            model.addAttribute("canChangeName", daysSinceLastChange >= 30);
            model.addAttribute("daysLeft", 30 - daysSinceLastChange);
        } else {
            model.addAttribute("canChangeName", true);
        }

        // MFA-related logic from MfaController
        if (!user.isMfaEnabled()) {
            if (user.getMfaSecret() == null || user.getMfaSecret().isEmpty()) {
                user.setMfaSecret(mfaService.generateNewSecret());
                userRepository.save(user);
            }
            model.addAttribute("qrCode", mfaService.generateQrCodeImageUri(user.getMfaSecret()));
        }

        return "settings";
    }
    
    // --- NEW: Step 1: Initiate Password Change (Validates old password and sends code) ---
    @PostMapping("/settings/change-password/initiate")
    public String initiatePasswordChange(@ModelAttribute("changePasswordDto") ChangePasswordDto dto, 
                                         RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            String token = userService.initiatePasswordChange(user, dto);
            
            // Pass the token to the view to conditionally show the second step
            redirectAttributes.addFlashAttribute("message", "A verification code has been sent to your email. Please enter it below to confirm your new password.");
            redirectAttributes.addFlashAttribute("verifyToken", token);
            
            return "redirect:/settings#security"; 
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
            redirectAttributes.addFlashAttribute("changePasswordDto", dto);
            return "redirect:/settings#security";
        }
    }
    
    // --- NEW: Step 2: Finalize Password Change (Verifies code and updates password) ---
    @PostMapping("/settings/change-password/finalize")
    public String finalizePasswordChange(@ModelAttribute("verifyPasswordChangeDto") VerifyPasswordChangeDto dto,
                                         RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            // Check if the token in the form matches the one saved in the user object
            if (user.getChangePasswordToken() == null || !user.getChangePasswordToken().equals(dto.getToken())) {
                 throw new RuntimeException("Invalid session or verification attempt. Please restart the password change process.");
            }
            
            userService.finalizePasswordChange(user, dto);
            
            redirectAttributes.addFlashAttribute("message", "Password changed successfully! You have been logged out for security purposes. Please log back in with your new password.");
            
            SecurityContextHolder.clearContext(); 
            return "redirect:/login?logout"; 
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to verify code: " + e.getMessage());
            // Restore token on failure so the user can re-enter the code on the security tab
            redirectAttributes.addFlashAttribute("verifyToken", dto.getToken());
            return "redirect:/settings#security";
        }
    }

    @PostMapping("/mfa-disable")
    public String disableMfa(RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "MFA has been disabled successfully.");
        return "redirect:/settings#security";
    }
}