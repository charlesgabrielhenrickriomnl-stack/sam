-- Insert roles
INSERT INTO roles (name) VALUES ('ROLE_TEACHER');
INSERT INTO roles (name) VALUES ('ROLE_STUDENT');
INSERT INTO roles (name) VALUES ('ROLE_DEPARTMENT');

-- Insert subjects
INSERT INTO subjects (code, description, lec, lab, units, grade, schedule)
VALUES 
('IT101', 'Introduction to IT', 3, 0, 3, '1.75', 'MWF 8:30 AM - 9:30 AM R 10:30 AM - 12:30 PM'),
('CS202', 'Data Structures', 2, 1, 3, '2.00', 'MW 1:30 PM - 3:00 PM'),
('MATE1', 'College Algebra', 3, 0, 3, '3.00', 'TTh 9:30 AM - 11:00 AM'),
('FIL12', 'Filipino Subject', 3, 0, 3, 'INC', 'F 3:30 PM - 5:30 PM'),
('PE301', 'Physical Fitness', 2, 0, 2, 'PASSED', 'Sat 7:30 AM - 9:30 AM');

-- Insert default department user with encoded password for 'departmentpass'
INSERT INTO users (username, email, password, enabled, first_name, last_name) VALUES 
('department', 'department@sam.edu', '$2a$10$YourHashedPasswordHere', true, 'Department', 'Admin');

-- Assign department user to ROLE_DEPARTMENT
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'department' AND r.name = 'ROLE_DEPARTMENT';