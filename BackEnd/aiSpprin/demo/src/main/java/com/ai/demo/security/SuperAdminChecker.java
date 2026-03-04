package com.ai.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminChecker {

    @Value("${app.super-admin.email-domains}")
    private String superAdminEmailDomains;

    @Value("${app.super-admin.emails:}")
    private String superAdminEmails;

    public boolean isSuperAdmin(String email) {
        if (email == null) return false;

        // check specific emails
        for (String adminEmail : superAdminEmails.split(",")) {
            if (email.equalsIgnoreCase(adminEmail.trim())) {
                return true;
            }
        }

        // check domains
        for (String domain : superAdminEmailDomains.split(",")) {
            if (email.toLowerCase().endsWith("@" + domain.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}