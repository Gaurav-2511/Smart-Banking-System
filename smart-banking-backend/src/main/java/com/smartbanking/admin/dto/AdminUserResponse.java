package com.smartbanking.admin.dto;

import com.smartbanking.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {

    private Long userId;
    private String name;
    private String email;
    private Role role;
    private int totalAccounts;
    private LocalDateTime createdAt;
}