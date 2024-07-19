package com.mteam.sleerenthome.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {

    private Long id;
    private String email;
    private String token;
    private String type = "Bearer";
    private List<String> roles;

    public JwtResponse(Long id, String email, String token, List<String> roles ) {
        this.roles = roles;
        this.token = token;
        this.email = email;
        this.id = id;
    }

}
