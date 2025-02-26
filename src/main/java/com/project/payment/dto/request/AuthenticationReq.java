package com.project.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationReq {

    @JsonAlias({"userName", "user_name","username"})
    @NotBlank(message = "username cannot be null or empty")
    private String username;

    @JsonAlias({"passWord", "pass_word","password"})
    @NotBlank(message = "password cannot be null or empty")
    private String password;
}
