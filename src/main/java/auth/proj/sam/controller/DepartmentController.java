package auth.proj.sam.controller;

import auth.proj.sam.config.CustomUserDetails;
import auth.proj.sam.dto.TeacherRegistrationDto;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.UserRepository;
import auth.proj.sam.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DepartmentController {

    private final UserService userService;
    private final UserRepository userRepository;

    // Mock list of available subjects/courses for assignment
    private final List<String> availableSubjects = Arrays.asList(
            "BSIT 101 - Intro to IT", 
            "CS 202 - Data Structures", 
            "CE 301 - Structural Design", 
            "PE 101 - Physical Education"
    );

    public DepartmentController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/department/dashboard")
    public String departmentDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User departmentUser = userDetails.getUser();
        model.addAttribute("user", departmentUser);
        
        long totalStudents = userService.countUsersByRole("ROLE_STUDENT");
        long activeFaculty = userService.countUsersByRole("ROLE_TEACHER");

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("activeFaculty", activeFaculty);
        
        return "department-dashboard";
    }
    
    @GetMapping("/department/teachers")
    public String showManageTeachers(Model model) {
        List<User> allUsers = userRepository.findAll();
        List<User> teachers = allUsers.stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_TEACHER")))
            .collect(Collectors.toList());
            
        model.addAttribute("teachers", teachers);
        
        return "manage-teachers"; 
    }
    
    // --- SHOW ASSIGNMENT MANAGEMENT PAGE (GET) ---
    @GetMapping("/department/manage-assignments")
    public String showManageAssignments(
            @RequestParam("teacherId") String teacherId, 
            Model model
    ) {
        User teacher = userRepository.findByUsername(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with ID: " + teacherId));

        model.addAttribute("teacher", teacher);
        model.addAttribute("availableSubjects", availableSubjects);

        // DUMMY: Mock assigned subjects based on whether the ID is default or not
        List<String> assignedSubjectsMock = teacherId.equals("22-1-99999") 
            ? Arrays.asList("BSIT 101 - Intro to IT", "CS 202 - Data Structures")
            : Arrays.asList("PE 101 - Physical Education");
            
        model.addAttribute("assignedSubjects", assignedSubjectsMock);

        return "manage-assignments"; 
    }
    
    // --- NEW: ASSIGN SUBJECT ACTION (POST) ---
    @PostMapping("/department/assign-subject")
    public String assignSubject(
            @RequestParam("teacherId") String teacherId,
            @RequestParam("subjectCode") String subjectCode,
            RedirectAttributes redirectAttributes
    ) {
        // --- DUMMY ASSIGNMENT LOGIC START ---
        if (subjectCode == null || subjectCode.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a subject to assign.");
        } else {
            // In a real application: Save the assignment to the database here.
            redirectAttributes.addFlashAttribute("message", 
                "SUCCESS: Subject '" + subjectCode + "' assigned to Teacher ID " + teacherId + " (Mock Action).");
        }
        // --- DUMMY ASSIGNMENT LOGIC END ---
        
        // Redirect back to the assignment page to see the updated list (in reality, you'd fetch the DB list here)
        redirectAttributes.addAttribute("teacherId", teacherId);
        return "redirect:/department/manage-assignments";
    }

    // --- NEW: REMOVE SUBJECT ACTION (POST) ---
    @PostMapping("/department/remove-subject")
    public String removeSubject(
            @RequestParam("teacherId") String teacherId,
            @RequestParam("subjectCode") String subjectCode,
            RedirectAttributes redirectAttributes
    ) {
        // --- DUMMY REMOVAL LOGIC START ---
        // In a real application: Delete the assignment from the database here.
        redirectAttributes.addFlashAttribute("message", 
            "SUCCESS: Subject '" + subjectCode + "' removed from Teacher ID " + teacherId + " (Mock Action).");
        // --- DUMMY REMOVAL LOGIC END ---
        
        redirectAttributes.addAttribute("teacherId", teacherId);
        return "redirect:/department/manage-assignments";
    }


    @GetMapping("/department/create-teacher")
    public String showCreateTeacherForm(Model model) {
        if (!model.containsAttribute("dto")) {
            model.addAttribute("dto", new TeacherRegistrationDto());
        }
        model.addAttribute("sections", availableSubjects); 
        return "create-teacher";
    }

    @PostMapping("/department/create-teacher")
    public String processCreateTeacherForm(@ModelAttribute("dto") TeacherRegistrationDto dto, RedirectAttributes redirectAttributes) {
        try {
            userService.registerTeacher(dto);
            redirectAttributes.addFlashAttribute("message", "Teacher account for " + dto.getFirstName() + " created successfully! Assignments can now be managed.");
            return "redirect:/department/create-teacher"; 
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create teacher: " + e.getMessage());
            redirectAttributes.addFlashAttribute("dto", dto);
            return "redirect:/department/create-teacher";
        }
    }
}