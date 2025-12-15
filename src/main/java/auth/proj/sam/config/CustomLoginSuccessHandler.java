package auth.proj.sam.config;

import auth.proj.sam.model.TrustedDevice;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.TrustedDeviceRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private TrustedDeviceRepository trustedDeviceRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // --- NEW: Check Registration Step for Students ---
        // Step 5 means complete registration
        if (user.getRegistrationStep() < 5) {
            boolean isStudent = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            
            if (isStudent) {
                // Redirect to the current onboarding step (e.g., /onboarding/step2)
                response.sendRedirect("/onboarding/step" + user.getRegistrationStep());
                return;
            }
        }
        // --- END NEW CHECK ---
        
        if (user.isMfaEnabled()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("remember-me".equals(cookie.getName())) {
                        Optional<TrustedDevice> trustedDevice = trustedDeviceRepository.findByToken(cookie.getValue());
                        if (trustedDevice.isPresent() && trustedDevice.get().getUser().getId().equals(userDetails.getUser().getId()) && trustedDevice.get().getExpiryDate().isAfter(LocalDateTime.now())) {
                            redirectToDashboard(response, authentication);
                            return;
                        }
                    }
                }
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            response.sendRedirect("/mfa-verify");
            return;
        }

        redirectToDashboard(response, authentication);
    }

    private void redirectToDashboard(HttpServletResponse response, Authentication authentication) throws IOException {
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            // Check for Department first
            if ("ROLE_DEPARTMENT".equals(auth.getAuthority())) {
                response.sendRedirect("/department/dashboard");
                return;
            }
            if ("ROLE_TEACHER".equals(auth.getAuthority())) {
                response.sendRedirect("/teacher/dashboard");
                return;
            }
        }
        response.sendRedirect("/student/dashboard");
    }
}