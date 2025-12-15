package auth.proj.sam.controller;

import auth.proj.sam.config.CustomUserDetails;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String UPLOAD_DIR;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/profile/upload-image")
    public String uploadProfileImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/settings#profile";
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            redirectAttributes.addFlashAttribute("error", "Invalid file type. Please upload a valid image (JPEG, PNG, GIF, etc.).");
            return "redirect:/settings#profile";
        }

        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                 fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.write(filePath, file.getBytes());

            user.setProfileImagePath("/uploads/" + uniqueFilename);
            userRepository.save(user);

            // --- NEW: Refresh the security principal ---
            updatePrincipal(user);

            redirectAttributes.addFlashAttribute("message", "Profile picture updated successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to upload image. The file may be too large.");
        }

        return "redirect:/settings#profile";
    }

    @PostMapping("/profile/change-name")
    public String changeDisplayName(@RequestParam("displayName") String displayName, RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getLastDisplayNameChange() != null) {
            long daysSinceLastChange = ChronoUnit.DAYS.between(user.getLastDisplayNameChange(), LocalDateTime.now());
            if (daysSinceLastChange < 30) {
                redirectAttributes.addFlashAttribute("error", "You can only change your name once every 30 days.");
                return "redirect:/settings#profile";
            }
        }

        user.setDisplayName(displayName);
        user.setLastDisplayNameChange(LocalDateTime.now());
        userRepository.save(user);
        
        updatePrincipal(user);

        redirectAttributes.addFlashAttribute("message", "Display name updated successfully!");
        return "redirect:/settings#profile";
    }

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
}