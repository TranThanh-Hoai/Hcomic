package com.comic.h.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BanUserRequest {
    @NotBlank(message = "Lý do khóa tài khoản không được để trống")
    private String reason;
}
