package auth.proj.sam.controller;

import auth.proj.sam.config.CustomUserDetails;
import auth.proj.sam.dto.EducationalInfoDto;
import auth.proj.sam.dto.FamilyInfoDto;
import auth.proj.sam.dto.OtherInfoDto;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.UserRepository;
import auth.proj.sam.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OnboardingController {

    private final UserService userService;
    private final UserRepository userRepository;

    public OnboardingController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    private User getUserFromPrincipal(CustomUserDetails userDetails) {
        // Fetches the latest state of the user from the database
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found in database."));
    }
    
    // NEW: Reusable method to update the Security Context Principal
    private void updatePrincipal(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails newPrincipal = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
            newPrincipal,
            authentication.getCredentials(),
            authentication.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    /**
     * Checks if the user is on the correct step. Redirects if they've skipped a step 
     * or if they are already completed.
     */
    private String checkProgress(User user, int expectedStep) {
        if (user.getRegistrationStep() < expectedStep) {
            // Redirect back to the step they missed
            return "redirect:/onboarding/step" + user.getRegistrationStep();
        }
        if (user.getRegistrationStep() == 5) {
            // Step 5 means complete, redirect to student dashboard
            return "redirect:/student/dashboard";
        }
        return null; // Continue to the expected page
    }
    
    // --- Step 2: Educational Information ---
    @GetMapping("/onboarding/step2")
    public String showStep2Form(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = getUserFromPrincipal(userDetails);
        String redirect = checkProgress(user, 2);
        if (redirect != null) return redirect;

        model.addAttribute("dto", new EducationalInfoDto());
        return "educational-info";
    }

    @PostMapping("/onboarding/step2")
    public String processStep2Form(@AuthenticationPrincipal CustomUserDetails userDetails, @ModelAttribute("dto") EducationalInfoDto dto, RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromPrincipal(userDetails);
            userService.updateEducationalInfo(user, dto);
            // No need to update principal yet, as the dashboard relies on step >= 5
            redirectAttributes.addFlashAttribute("message", "Educational information saved. Proceed to Step 3.");
            return "redirect:/onboarding/step3";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving data: " + e.getMessage());
            return "redirect:/onboarding/step2";
        }
    }

    // --- Step 3: Family Information ---
    @GetMapping("/onboarding/step3")
    public String showStep3Form(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = getUserFromPrincipal(userDetails);
        String redirect = checkProgress(user, 3);
        if (redirect != null) return redirect;

        model.addAttribute("dto", new FamilyInfoDto());
        return "family-info";
    }

    @PostMapping("/onboarding/step3")
    public String processStep3Form(@AuthenticationPrincipal CustomUserDetails userDetails, @ModelAttribute("dto") FamilyInfoDto dto, RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromPrincipal(userDetails);
            userService.updateFamilyInfo(user, dto);
            // No need to update principal yet, as the dashboard relies on step >= 5
            redirectAttributes.addFlashAttribute("message", "Family information saved. Proceed to Step 4.");
            return "redirect:/onboarding/step4";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving data: " + e.getMessage());
            return "redirect:/onboarding/step3";
        }
    }

    // --- Step 4: Other Information (Final Step) ---
    @GetMapping("/onboarding/step4")
    public String showStep4Form(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = getUserFromPrincipal(userDetails);
        String redirect = checkProgress(user, 4);
        if (redirect != null) return redirect;
        
        model.addAttribute("dto", new OtherInfoDto());
        model.addAttribute("genders", User.Gender.values());
        model.addAttribute("civilStatuses", User.CivilStatus.values());
        model.addAttribute("citizenships", User.Citizenship.values());
        
        return "other-info";
    }

    @PostMapping("/onboarding/step4")
    public String processStep4Form(@AuthenticationPrincipal CustomUserDetails userDetails, @ModelAttribute("dto") OtherInfoDto dto, RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromPrincipal(userDetails);
            userService.updateOtherInfo(user, dto);
            
            // FIX: Refresh the authentication principal right after the last step is saved.
            User updatedUser = getUserFromPrincipal(userDetails);
            updatePrincipal(updatedUser); 
            
            redirectAttributes.addFlashAttribute("message", "Registration complete! Welcome to the dashboard.");
            return "redirect:/student/dashboard";
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("error", "Error saving data: " + e.getMessage());
            return "redirect:/onboarding/step4";
        }
    }
}