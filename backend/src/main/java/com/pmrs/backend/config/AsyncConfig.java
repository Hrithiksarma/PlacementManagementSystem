package com.pmrs.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async} so selection emails send in the background —
 * clicking "Selected" returns immediately instead of waiting on SMTP.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
