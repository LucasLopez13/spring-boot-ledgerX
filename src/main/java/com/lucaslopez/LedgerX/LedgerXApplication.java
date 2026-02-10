package com.lucaslopez.LedgerX;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class LedgerXApplication {

	public static void main(String[] args) {
		SpringApplication.run(LedgerXApplication.class, args);
	}

}
