package com.project.payment.dto.response;

import com.project.payment.dao.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * DTO for {@link User}
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRes extends BaseRes implements Serializable {

    //@NotBlank(message = "user id cannot be null or empty")
    private String userId;
}