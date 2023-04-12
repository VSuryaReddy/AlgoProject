package com.algo.algoMain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.algo")
@EntityScan("com.algo")
@EnableJpaRepositories("com.algo")
public class AlgoProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlgoProjectApplication.class, args);
	}

}
