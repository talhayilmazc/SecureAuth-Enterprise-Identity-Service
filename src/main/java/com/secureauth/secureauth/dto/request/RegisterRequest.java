package com.secureauth.secureauth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3-50 karakter olmalıdır")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Kullanıcı adı sadece harf, rakam ve alt çizgi içerebilir")
    private String username;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir")
    private String password;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(max = 50)
    private String lastName;
}