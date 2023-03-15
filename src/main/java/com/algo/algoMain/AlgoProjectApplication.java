package com.algo.algoMain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.algo")
public class AlgoProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlgoProjectApplication.class, args);
	}

}
