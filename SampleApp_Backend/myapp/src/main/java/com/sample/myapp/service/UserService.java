package com.sample.myapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sample.myapp.dto.AuthResponseDto;
import com.sample.myapp.dto.LoginRequestDto;
import com.sample.myapp.dto.UserRegistrationDto;
import com.sample.myapp.exception.ResourceAlreadyExistsException;
import com.sample.myapp.exception.ResourceNotFoundException;
import com.sample.myapp.model.User;
import com.sample.myapp.repository.UserRepository;
import com.sample.myapp.util.JwtUtil;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponseDto registerUser(UserRegistrationDto registrationDto) {
        // Check if user already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already in use");
        }
        
        // Create new user
        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        // Return response
        return new AuthResponseDto(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            token
        );
    }
    
    public AuthResponseDto loginUser(LoginRequestDto loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));
        
        // Check password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        
        // Return response
        return new AuthResponseDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            token
        );
    }
}

