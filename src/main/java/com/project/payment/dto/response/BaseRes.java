package com.project.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.payment.constant.CurrencyEnum;
import com.project.payment.dao.entity.Role;
import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public abstract class BaseRes {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String firstname;

    private String lastname;

    private String email;

    private String accountNumber;
    private BigDecimal balance;
    private CurrencyEnum currency;

    @JsonIgnore
    @ToString.Exclude
    private Set<Role> roles;

    private Set<String> assignedRoles;

    public Set<String> getAssignedRoles() {
        if (assignedRoles == null && roles != null) {
            assignedRoles = new HashSet<>();
            roles.forEach(userRole -> assignedRoles.add(userRole.getName()));
        }
        return assignedRoles != null ? assignedRoles : new HashSet<>();
    }
}
