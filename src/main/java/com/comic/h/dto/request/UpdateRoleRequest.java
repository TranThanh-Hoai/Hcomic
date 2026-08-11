package com.comic.h.dto.request;

import com.comic.h.enums.Role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    @NotNull(message = "Vai trò không được để trống")
    private Role role;
}
