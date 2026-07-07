package com.example.english_learning_platform;

import com.example.english_learning_platform.service.CourseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EnglishLearningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnglishLearningPlatformApplication.class, args);
    }

    @Bean
    CommandLineRunner seedCourses(CourseService courseService) {
        return args -> courseService.seedCourseData();
    }
}
