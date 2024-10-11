// Declare the package for this class, indicating it's part of the 'p2p' module within 'example.com'
package com.example.p2p;

// Import necessary classes from Spring Boot framework
import org.springframework.boot.SpringApplication; // Class to launch a Spring application
import org.springframework.boot.autoconfigure.SpringBootApplication; // Annotation to mark the main class of a Spring Boot application

// Annotate the class with @SpringBootApplication, which combines three annotations:
// @Configuration (for Spring configuration), @EnableAutoConfiguration (for auto-configuring beans), and @ComponentScan (to find Spring components)
@SpringBootApplication
public class P2PSystemApplication {
    // The main method, which serves as the entry point for the application
    public static void main(String[] args) {
        // Run the Spring application using the specified class and command-line arguments
        SpringApplication.run(P2PSystemApplication.class, args);
    }
}
