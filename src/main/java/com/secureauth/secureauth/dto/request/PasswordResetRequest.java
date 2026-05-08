package com.secureauth.secureauth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequest {

    @NotBlank(message = "Token boş olamaz")
    private String token;

    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir")
    private String newPassword;
}