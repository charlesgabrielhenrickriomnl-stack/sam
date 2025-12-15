package auth.proj.sam.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller advice to add global attributes to the model for use in templates.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Adds the current request URI to the model for all requests.
     * This is used by Thymeleaf templates to determine the active page for navigation.
     * @param request The HttpServletRequest object provided by Spring.
     * @return The request URI string (e.g., "/dashboard").
     */
    @ModelAttribute("requestURI")
    public String requestURI(final HttpServletRequest request) {
        return request.getRequestURI();
    }
}
