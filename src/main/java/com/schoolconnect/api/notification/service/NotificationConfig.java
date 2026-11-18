package com.schoolconnect.api.notification.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Active les tâches planifiées (relances automatiques des impayés, CU-SYS-03). */
@Configuration
@EnableScheduling
public class NotificationConfig {
}
