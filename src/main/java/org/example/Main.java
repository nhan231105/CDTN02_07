package org.example;

import campuswork.seed.SeedCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@ComponentScan(basePackages = "org.example")
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--campuswork-seed".equals(args[0])) {
            SeedCommand.run(java.util.Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        SpringApplication.run(Main.class, args);
    }
}

