package auth.proj.sam.controller;

import auth.proj.sam.dto.StudentRegistrationDto;
import auth.proj.sam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        if (!model.containsAttribute("studentDto")) {
            model.addAttribute("studentDto", new StudentRegistrationDto());
        }
        return "login";
    }

    @PostMapping("/register/student")
    public String processStudentRegistration(@ModelAttribute("studentDto") StudentRegistrationDto studentDto, RedirectAttributes redirectAttributes, Model model) {
        try {
            String studentId = userService.registerStudent(studentDto);
            
            // FIX: Redirect to /verify and pass the Student ID (username)
            redirectAttributes.addFlashAttribute("message", "Registration successful! A verification code has been sent to your email: " + studentDto.getEmail() + ". Please enter it below to verify your account.");
            redirectAttributes.addAttribute("username", studentId); // Passes the ID to the /verify controller
            return "redirect:/verify"; 
            
        } catch (RuntimeException e) {
            // Failure: Stay on the current page (forward) and display error
            
            // 1. Add the error message directly to the model (since we are forwarding)
            model.addAttribute("registrationError", "Registration failed: " + e.getMessage());
            
            // 2. Forward back to the 'login' view.
            return "login"; 
        }
    }

    @GetMapping("/verify")
    public String showVerificationForm(@RequestParam(value = "username", required = false) String username, Model model) {
        model.addAttribute("username", username);
        return "verify";
    }

    @PostMapping("/verify")
    public String verifyUser(@RequestParam("username") String username, @RequestParam("code") String code, RedirectAttributes redirectAttributes) {
        if (userService.verifyUser(username, code)) {
            redirectAttributes.addFlashAttribute("message", "Your account has been verified successfully! Your **Student ID and initial password** have been sent to your email. Please login to complete your profile registration.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired verification code.");
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/verify";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, HttpServletRequest request, Model model) {
        try {
            String siteURL = request.getRequestURL().toString().replace(request.getServletPath(), "");
            userService.generatePasswordResetToken(email, siteURL);
            model.addAttribute("message", "A password reset link has been sent to your email.");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (userService.validatePasswordResetToken(token)) {
            model.addAttribute("token", token);
            return "reset-password";
        } else {
            return "redirect:/login?error=invalidToken";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("message", "You have successfully reset your password. Please log in.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired token.");
            return "redirect:/reset-password?token=" + token;
        }
    }
}