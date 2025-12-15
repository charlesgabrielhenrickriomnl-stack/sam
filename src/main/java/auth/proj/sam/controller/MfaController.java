package auth.proj.sam.controller;

import auth.proj.sam.model.TrustedDevice;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.TrustedDeviceRepository;
import auth.proj.sam.repository.UserRepository;
import auth.proj.sam.service.MfaService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;

    public MfaController(MfaService mfaService, UserRepository userRepository, TrustedDeviceRepository trustedDeviceRepository) {
        this.mfaService = mfaService;
        this.userRepository = userRepository;
        this.trustedDeviceRepository = trustedDeviceRepository;
    }

    @PostMapping("/mfa-enable")
    public String enableMfa(@RequestParam String code, RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (mfaService.isTotpValid(user.getMfaSecret(), code)) {
            user.setMfaEnabled(true);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "MFA has been enabled successfully!");
            return "redirect:/settings#security";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid MFA code. Please try again.");
            return "redirect:/settings#security";
        }
    }

    @GetMapping("/mfa-verify")
    public String showMfaVerifyPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        model.addAttribute("username", auth.getName());
        return "mfa-verify";
    }

    @PostMapping("/mfa-verify")
    public String verifyMfaCode(@RequestParam String username, @RequestParam String code, @RequestParam(name = "trustDevice", required = false) boolean trustDevice,
                                HttpServletResponse response, RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (mfaService.isTotpValid(user.getMfaSecret(), code)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Authentication fullyAuthenticated = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), auth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(fullyAuthenticated);

            if (trustDevice) {
                String token = UUID.randomUUID().toString();
                TrustedDevice newTrustedDevice = new TrustedDevice();
                newTrustedDevice.setUser(user);
                newTrustedDevice.setToken(token);
                newTrustedDevice.setExpiryDate(LocalDateTime.now().plusDays(30));
                trustedDeviceRepository.save(newTrustedDevice);

                Cookie cookie = new Cookie("remember-me", token);
                cookie.setHttpOnly(true);
                cookie.setMaxAge(30 * 24 * 60 * 60);
                cookie.setPath("/");
                response.addCookie(cookie);
            }

            for (var authority : auth.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_TEACHER")) {
                    return "redirect:/teacher/dashboard";
                }
            }
            return "redirect:/student/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid MFA code.");
            return "redirect:/mfa-verify";
        }
    }
}