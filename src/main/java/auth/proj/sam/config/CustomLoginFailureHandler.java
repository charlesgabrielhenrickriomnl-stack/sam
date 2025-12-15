package auth.proj.sam.config;

import auth.proj.sam.model.User;
import auth.proj.sam.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Autowired
    public CustomLoginFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String errorMessage = "Invalid username or password.";

        // NEW: Check for DisabledException (unverified account)
        if (exception instanceof DisabledException) {
            errorMessage = "Your account is not verified. Please check your email for a verification link or contact customer support.";
        } else if (exception instanceof LockedException) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getAccountStatus() == User.AccountStatus.TIMED_OUT && user.getTimeoutUntil() != null) {
                long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getTimeoutUntil());
                long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getTimeoutUntil());

                if (secondsLeft > 0) {
                     errorMessage = "Your account is in a timeout. Please try again in " + (minutesLeft + 1) + " minutes.";
                } else {
                     errorMessage = "Your timeout has expired. Please try logging in again.";
                }

            } else {
                errorMessage = "Your account is locked.";
            }
        }

        setDefaultFailureUrl("/login?error=" + errorMessage);
        super.onAuthenticationFailure(request, response, exception);
    }
}