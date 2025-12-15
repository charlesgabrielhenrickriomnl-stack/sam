package auth.proj.sam.controller;

import auth.proj.sam.config.CustomUserDetails;
import auth.proj.sam.model.User;
import auth.proj.sam.model.Subject;
import auth.proj.sam.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ScheduleController {

    @Autowired
    private SubjectRepository subjectRepository;

    // Time slots for the schedule grid
    private List<String> getTimeSlots() {
        return Arrays.asList(
            "07:30 AM", "08:30 AM", "09:30 AM", "10:30 AM", 
            "11:30 AM", "12:30 PM", "01:30 PM", "02:30 PM", 
            "03:30 PM", "04:30 PM", "05:30 PM", "06:30 PM", 
            "07:30 PM", "08:30 PM", "09:30 PM"
        );
    }

    // Convert Subject entities to Map format for template compatibility
    private List<Map<String, String>> convertSubjectsToMap(List<Subject> subjects) {
        return subjects.stream().map(subject -> Map.of(
            "code", subject.getCode(),
            "description", subject.getDescription(),
            "lec", String.valueOf(subject.getLec()),
            "lab", String.valueOf(subject.getLab()),
            "units", String.valueOf(subject.getUnits()),
            "grade", subject.getGrade(),
            "schedule", subject.getSchedule()
        )).collect(Collectors.toList());
    }

    /**
     * Shows the dedicated schedule grid page.
     */
    @GetMapping("/student/schedule")
    public String showSchedulePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("subjects", convertSubjectsToMap(subjectRepository.findAll())); // Fetch from DB
        model.addAttribute("timeSlots", getTimeSlots());
        // Use user's current academic status for context.
        model.addAttribute("schoolYear", currentUser.getSchoolYear() != null ? currentUser.getSchoolYear() : "N/A");
        model.addAttribute("term", currentUser.getTerm() != null ? currentUser.getTerm() : "N/A");
        
        return "schedule"; // Points to the schedule-only template
    }
    
    /**
     * Shows the dedicated grades (subject list) page.
     */
     @GetMapping("/student/grades")
     public String showGradesPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("subjects", convertSubjectsToMap(subjectRepository.findAll())); // Fetch from DB
        // Use user's current academic status for context.
        model.addAttribute("schoolYear", currentUser.getSchoolYear() != null ? currentUser.getSchoolYear() : "N/A");
        model.addAttribute("term", currentUser.getTerm() != null ? currentUser.getTerm() : "N/A");
        
        return "grades"; // Points to the grades-only template
    }
}