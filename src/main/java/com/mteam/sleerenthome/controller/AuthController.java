package com.mteam.sleerenthome.controller;

import com.mteam.sleerenthome.exception.UserAlreadyExistsException;
import com.mteam.sleerenthome.model.User;
import com.mteam.sleerenthome.request.LoginRequest;
import com.mteam.sleerenthome.response.JwtResponse;
import com.mteam.sleerenthome.security.jwt.JwtUtils;
import com.mteam.sleerenthome.security.user.CustomUserDetails;
import com.mteam.sleerenthome.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LogManager.getLogger(RoomController.class);

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        logger.info("registerUser...{}, {}, {}, {}", user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        try {
            userService.resisterUser(user);
            return ResponseEntity.ok("registration successfully");
        } catch (UserAlreadyExistsException ue) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ue.getMessage());

        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("authenticateUser...{}, {}", loginRequest.getEmail(), loginRequest.getPassword());
        // 사용자 인증
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // JWT 토큰 생성
        String jwt = jwtUtils.generateJwtTokenForUser(authentication);
        // 사용자 세부 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // 역할 가져오기
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return ResponseEntity.ok(new JwtResponse(
                userDetails.getId(),
                userDetails.getEmail(),
                jwt,
                roles));

    }


    @GetMapping("/generator-key")
    public ResponseEntity<?> generatorJwtSecretKey() {
        return ResponseEntity.ok(jwtUtils.generatorJwtSecretKey());
    }
}
