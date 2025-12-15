package auth.proj.sam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Inject the upload directory path from application.properties
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps the URL path "/uploads/**" to the external directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
    
    // NEW METHOD to redirect the root path
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Add a view controller to redirect the root path to /login
        registry.addViewController("/").setViewName("redirect:/login");
    }
}