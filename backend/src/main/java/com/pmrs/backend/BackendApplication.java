package com.pmrs.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackendApplication {

	private static final String HASH_FLAG = "--generate-hash=";

	public static void main(String[] args) {
		// One-off local utility: print a BCrypt hash for a password and exit,
		// without booting the full Spring context. Used to bootstrap the admin
		// row directly in MySQL — never send a real password to a web-based
		// bcrypt generator.
		for (String arg : args) {
			if (arg.startsWith(HASH_FLAG)) {
				String rawPassword = arg.substring(HASH_FLAG.length());
				System.out.println(new BCryptPasswordEncoder().encode(rawPassword));
				return;
			}
		}

		SpringApplication.run(BackendApplication.class, args);
	}

}
