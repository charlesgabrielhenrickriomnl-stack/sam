package auth.proj.sam.controller;

import auth.proj.sam.config.CustomUserDetails;
import auth.proj.sam.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class ScheduleController {

    /**
     * IMPORTANT: This is DUMMY DATA. In a real system, this data should be fetched
     * from a service that queries the academic database based on the student's ID,
     * current school year, and term.
     */
    private List<Map<String, String>> getSampleSubjects() {
        return Arrays.asList(
            Map.of("code", "IT101", "description", "Introduction to IT", "lec", "3", "lab", "0", "units", "3", "grade", "1.75", "schedule", "MWF 8:30 AM - 9:30 AM R 10:30 AM - 12:30 PM"),
            Map.of("code", "CS202", "description", "Data Structures", "lec", "2", "lab", "1", "units", "3", "grade", "2.00", "schedule", "MW 1:30 PM - 3:00 PM"),
            Map.of("code", "MATE1", "description", "College Algebra", "lec", "3", "lab", "0", "units", "3", "grade", "3.00", "schedule", "TTh 9:30 AM - 11:00 AM"),
            Map.of("code", "FIL12", "description", "Filipino Subject", "lec", "3", "lab", "0", "units", "3", "grade", "INC", "schedule", "F 3:30 PM - 5:30 PM"),
            Map.of("code", "PE301", "description", "Physical Fitness", "lec", "2", "lab", "0", "units", "2", "grade", "PASSED", "schedule", "Sat 7:30 AM - 9:30 AM")
        );
    }
    
    // Time slots for the schedule grid
    private List<String> getTimeSlots() {
        return Arrays.asList(
            "07:30 AM", "08:30 AM", "09:30 AM", "10:30 AM", 
            "11:30 AM", "12:30 PM", "01:30 PM", "02:30 PM", 
            "03:30 PM", "04:30 PM", "05:30 PM", "06:30 PM", 
            "07:30 PM", "08:30 PM", "09:30 PM"
        );
    }

    /**
     * Shows the dedicated schedule grid page.
     */
    @GetMapping("/student/schedule")
    public String showSchedulePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("subjects", getSampleSubjects()); // Dummy Subjects
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
        model.addAttribute("subjects", getSampleSubjects()); // Dummy Subjects
        // Use user's current academic status for context.
        model.addAttribute("schoolYear", currentUser.getSchoolYear() != null ? currentUser.getSchoolYear() : "N/A");
        model.addAttribute("term", currentUser.getTerm() != null ? currentUser.getTerm() : "N/A");
        
        return "grades"; // Points to the grades-only template
    }
}