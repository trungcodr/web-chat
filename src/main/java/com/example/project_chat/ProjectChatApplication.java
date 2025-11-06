package com.example.project_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.example.project_chat.repository")
@SpringBootApplication
public class ProjectChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectChatApplication.class, args);
	}

}
