package com.pursuitly.pursuitly.auth;

import com.pursuitly.pursuitly.auth.dto.LoginRequest;
import com.pursuitly.pursuitly.auth.dto.RegisterRequest;
import com.pursuitly.pursuitly.user.UserService;
import com.pursuitly.pursuitly.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request.getEmail(), request.getFullName(), request.getPassword());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userService.getUserByEmail(request.getEmail());
        String token = jwtUtil.generateToken(request.getEmail(), user.getRole());
        return ResponseEntity.ok(token);
    }
}
