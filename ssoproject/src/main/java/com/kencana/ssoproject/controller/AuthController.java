package com.kencana.ssoproject.controller;

import com.kencana.ssoproject.config.JwtUtils; // Pastikan import JwtUtils
import com.kencana.ssoproject.model.User;
import com.kencana.ssoproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils; // Gunakan JwtUtils, bukan JwtService terpisah

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username sudah digunakan!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            // Memanggil generateJwtToken dari JwtUtils yang kuncinya sudah pasti sama
            String token = jwtUtils.generateJwtToken(userOpt.get().getUsername());
            return ResponseEntity.ok().body("{\"token\": \"" + token + "\", \"message\": \"Login Berhasil!\"}");
        }
        return ResponseEntity.status(401).body("Error: Username atau password salah!");
    }
}