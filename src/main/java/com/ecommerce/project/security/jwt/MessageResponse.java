package com.ecommerce.project.security.jwt;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class MessageResponse {
    private String message;

    public MessageResponse(String s) {
        this.message = s;
    }
}
