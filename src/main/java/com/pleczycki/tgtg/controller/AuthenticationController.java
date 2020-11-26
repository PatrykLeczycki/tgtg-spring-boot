package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.dto.UserDto;

import com.pleczycki.tgtg.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto userDto) {
        ResponseEntity<?> register = authenticationService.register(userDto);
        if (register.getStatusCodeValue() == 200) {
            new Thread(() -> authenticationService.sendRegistrationEmail(userDto.getEmail())).start();
        }
        return register;
    }

    @PostMapping("/confirmAccount")
    public ResponseEntity<?> confirmAccount(@RequestBody Map<String, String> accountConfirmationData) {
        return authenticationService.confirmAccount(accountConfirmationData);
    }

    @PostMapping("/resendConfirmationLink")
    public ResponseEntity<?> resendConfirmationLink(@RequestBody Map<String, String> resendConfirmationLinkData) {
        return authenticationService.resendConfirmationLink(resendConfirmationLinkData);
    }

    @PostMapping("/retrievePasswordFirst")
    public ResponseEntity<?> retrievePasswordFirstStage(@RequestBody Map<String, String> emailData) {
        ResponseEntity<?> retrieveResponse = authenticationService.retrievePassword(emailData.get("email"));
        if (retrieveResponse.getStatusCodeValue() == 200) {
            new Thread(() -> authenticationService.sendPasswordRecoveryEmail(emailData.get("email"))).start();
        }
        return retrieveResponse;
    }

    @PostMapping("/retrievePasswordSecond")
    public ResponseEntity<?> retrievePasswordSecondStage(@RequestBody Map<String, String> passwordRetrievalData) {
        return authenticationService.retrievePassword(passwordRetrievalData);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> changePasswordData) {
        return authenticationService.changePassword(changePasswordData);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto userDto) {
        return authenticationService.login(userDto);
    }
}
