package auth.proj.sam.controller;

import auth.proj.sam.config.CustomUserDetails;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.UserRepository;
import auth.proj.sam.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserService userService;
    private final UserRepository userRepository;

    public DashboardController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }
    
    // --- DUMMY DATA FOR TEACHER'S SUBJECTS AND STUDENTS ---
    private List<Map<String, String>> getTeacherSubjects(User teacher) {
        // Dummy data: assume teacher teaches these subjects.
        return Arrays.asList(
            Map.of("code", "IT101", "description", "Introduction to IT", "schedule", "MWF 8:30 AM", "count", "35"),
            Map.of("code", "CS202", "description", "Data Structures", "schedule", "MW 1:30 PM", "count", "30")
        );
    }
    
    // FIX: ADDED "grade" FIELD TO EACH MAP
    private List<Map<String, String>> getStudentsForSubject(String subjectCode) {
        // Dummy data: a list of students in one of the teacher's classes.
        if ("IT101".equals(subjectCode)) {
            return Arrays.asList(
                Map.of("id", "22-1-00001", "name", "John Smith", "course", "BSIT 1-1", "grade", "1.75"),
                Map.of("id", "22-1-00002", "name", "Jane Doe", "course", "BSIT 1-1", "grade", "2.00"),
                Map.of("id", "22-1-00003", "name", "Alice Brown", "course", "BSCS 2-1", "grade", "INC"),
                Map.of("id", "22-1-00004", "name", "Bob Green", "course", "BSCS 2-1", "grade", "2.50"),
                Map.of("id", "22-1-00005", "name", "Eva White", "course", "BSIT 1-2", "grade", "1.00")
            );
        } else {
             return Collections.emptyList();
        }
    }
    // --- END DUMMY DATA ---

    @GetMapping("/student/dashboard")
    public String studentDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();
        model.addAttribute("user", currentUser);

        if (currentUser.getAccountStatus() == User.AccountStatus.TIMED_OUT && currentUser.getTimeoutUntil() != null) {
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), currentUser.getTimeoutUntil());
            model.addAttribute("timeoutMinutes", Math.max(0, minutesLeft));
        }
        return "student-dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User teacher = userDetails.getUser();
        model.addAttribute("user", teacher); 
        
        // Data for the teacher dashboard content
        List<Map<String, String>> subjects = getTeacherSubjects(teacher);
        model.addAttribute("subjects", subjects);
        
        // Add one specific subject's student list as a dummy block
        model.addAttribute("subjectStudents", getStudentsForSubject("IT101")); 
        model.addAttribute("subjectTitle", "IT101 - Introduction to IT");

        return "teacher-dashboard"; 
    }
    
    /**
     * Shows the dedicated student list page for the teacher.
     */
    @GetMapping("/teacher/students")
    public String showManageStudentsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User teacher = userDetails.getUser();
        model.addAttribute("user", teacher);
        
        // DUMMY: Assume the teacher is assigned to IT101 students
        List<Map<String, String>> students = getStudentsForSubject("IT101");
        
        model.addAttribute("students", students);
        model.addAttribute("course", "IT101 - Introduction to IT");
        model.addAttribute("totalStudents", students.size());
        
        return "manage-teacher-students"; 
    }
}