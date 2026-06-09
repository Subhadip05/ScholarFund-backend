package com.scholarfund.backend.dto;

import lombok.Data;

@Data
public class UserSearchInput {
    private String email;
    private String fullName;
    private String phoneNumber;
    private Integer roleId;
    private Boolean isVerified;
    private Boolean isActive;
}