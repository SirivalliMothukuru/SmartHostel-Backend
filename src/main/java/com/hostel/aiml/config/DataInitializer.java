
package com.hostel.aiml.config;

import com.hostel.aiml.entity.Student;
import com.hostel.aiml.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner addSampleStudents(
            StudentRepository repository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String[][] students = {
                {"Suharsha", "AIML003", "suharsha"},
                {"Jathin", "AIML004", "jathin"},
                {"Charitha", "AIML005", "charitha"},
                {"Pavani", "AIML006", "pavani"},
                {"Harika", "AIML007", "harika"},
                {"Adharsh", "AIML008", "adharsh"},
                {"Hasini", "AIML009", "hasini"},
                {"Varun", "AIML010", "varun"},
                {"Rithika", "AIML011", "rithika"},
                {"Nikitha", "AIML012", "nikitha"},
                {"Manasa", "AIML013", "manasa"}
            };

            for (String[] item : students) {
                String email = item[2] + ".test@smarthostel.local";

                if (!repository.existsByEmail(email)) {
                    Student student = new Student();
                    student.setName(item[0]);
                    student.setRollNumber(item[1]);
                    student.setEmail(email);
                    student.setPhone(null);
                    student.setPassword(
                        passwordEncoder.encode("ChangeMe123!")
                    );

                    repository.save(student);
                }
            }

            System.out.println("Sample students initialized.");
        };
    }
}
