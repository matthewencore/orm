package com.acme.orm.web.dto;

import com.acme.orm.domain.enums.UserRole;

public record UserSummary(Long id, String name, String email, UserRole role) {
}

