package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.dto.UserDto;
import com.pleczycki.tgtg.model.RoleName;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.RoleRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.security.JwtAuthenticationResponse;
import com.pleczycki.tgtg.security.JwtTokenProvider;
import com.pleczycki.tgtg.utils.ApiResponse;
import com.pleczycki.tgtg.utils.CustomModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import javax.validation.Valid;

@Service("AuthenticationService")
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomModelMapper modelMapper;

    public ResponseEntity<?> register(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email is already used!"),
                    HttpStatus.BAD_REQUEST);
        } else {
            User user = modelMapper.map(userDto, User.class);
            user.setUsername("");
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(
                    () -> new RuntimeException("User Role not set.")
            )));
            userRepository.save(user);
            return new ResponseEntity(new ApiResponse(true, "User registered successfully"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> login(@Valid @RequestBody UserDto userDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDto.getEmail(),
                        userDto.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }
}
