package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.config.EnvConfig;
import com.pleczycki.tgtg.dto.UserDto;
import com.pleczycki.tgtg.model.RoleName;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.RoleRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.security.JwtAuthenticationResponse;
import com.pleczycki.tgtg.security.JwtTokenProvider;
import com.pleczycki.tgtg.utils.ApiResponse;
import com.pleczycki.tgtg.utils.CustomModelMapper;
import com.pleczycki.tgtg.utils.Mailer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import javax.transaction.Transactional;
import javax.validation.Valid;

@Slf4j
@Service("AuthenticationService")
public class AuthenticationService {

    @Autowired
    private EnvConfig envConfig;

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

    @Autowired
    private Mailer mailer;

    @Transactional
    public ResponseEntity<ApiResponse> register(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Email is already used!"));
        }

        User user = modelMapper.map(userDto, User.class);
        user.setCreatedAt(new Date());
        user.setUsername("");
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(
                () -> new RuntimeException("User Role not set.")
        )));
        user.setEnabled(false);
        user.setRegistrationToken(RandomStringUtils.randomAlphanumeric(30));
        User save = userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
    }

    public ResponseEntity<JwtAuthenticationResponse> login(@Valid @RequestBody UserDto userDto) {
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

    public ResponseEntity<ApiResponse> confirmAccount(Map<String, String> confirmationData) {
        Long id = Long.valueOf(confirmationData.get("userId"));
        String token = confirmationData.get("registrationToken");
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "E-mail not found"));
        }

        User user = optionalUser.get();

        if (user.getRegistrationToken().equals(token)) {
            user.setRegistrationToken(null);
            user.setEnabled(true);
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse(true, "Account activated successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Invalid confirmation token"));
    }

    public ResponseEntity<ApiResponse> resendConfirmationLink(Map<String, String> resendConfirmationLinkData) {
        String email = resendConfirmationLinkData.get("email");
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "E-mail not found"));
        }

        User user = optionalUser.get();

        if (!user.isEnabled()) {
            user.setRegistrationToken(RandomStringUtils.randomAlphanumeric(30));
            User save = userRepository.save(user);
            new Thread(() -> resendConfirmationLinkEmail(save.getEmail())).start();
            return ResponseEntity.ok(new ApiResponse(true, "Confirmation link resent successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User is already activated"));
    }

    @Transactional
    public ResponseEntity<ApiResponse> retrievePassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "E-mail not found"));
        }

        User user = optionalUser.get();
        user.setPassRecoveryToken(RandomStringUtils.randomAlphanumeric(30));
        return ResponseEntity.ok(new ApiResponse(true, "Message with password retrieve link sent successfully"));
    }

    @Transactional
    public ResponseEntity<ApiResponse> retrievePassword(Map<String, String> passwordRetrievalData) {

        Optional<User> optionalUser = userRepository.findById(Long.valueOf(passwordRetrievalData.get("userId")));
        String token = passwordRetrievalData.get("lostPasswordToken");

        if(optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid password recovery token - user not found."));
        }

        User user = optionalUser.get();

        if (user.getPassRecoveryToken().equals(token)) {
            user.setPassword(passwordEncoder.encode(passwordRetrievalData.get("password")));
            user.setPassRecoveryToken(null);
            new Thread(() -> sendPasswordChangeEmail(user.getEmail())).start();
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, "Invalid password recovery token"));
    }

    public ResponseEntity<ApiResponse> changePassword(Map<String, String> changePasswordData) {
        Long userId = Long.valueOf(changePasswordData.get("userId"));
        String currentPassword = changePasswordData.get("currentPassword");
        String newPassword = changePasswordData.get("newPassword");

        Optional<User> optionalUser = userRepository.findById(userId);

        if(optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "User not found."));
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        new Thread(() -> sendPasswordChangeEmail(user.getEmail())).start();
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    public void sendRegistrationEmail(String recipientEmail) {

        Optional<User> optionalUser = userRepository.findByEmail(recipientEmail);

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User with given e-mail address not found");
        }

        User user = optionalUser.get();
        String url = envConfig.getWebsiteUrl() +
                "/confirmAccount?userId=" + user.getId() + "&token=" + user.getRegistrationToken();
        String firstParagraph = "Dziękujemy za zarejestrowanie się na naszej stronie. Prosimy o wejście w poniższy link w celu dokończenia procesu rejestracji:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne.";
        String buttonLabel = "Potwierdzenie rejestracji";
        String subject = "Rejestracja w Too Good To Go";

        try {
            sendEmail(recipientEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void resendConfirmationLinkEmail(String recipientEmail) {

        Optional<User> optionalUser = userRepository.findByEmail(recipientEmail);

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User with given e-mail address not found");
        }

        User user = optionalUser.get();
        String url = envConfig.getWebsiteUrl() +
                "/confirmAccount?userId=" + user.getId() + "&token=" + user.getRegistrationToken();
        String firstParagraph = "Otrzymałeś tę wiadomość, ponieważ podczas prośby o ponowne wysłanie linku aktywacyjnego został podany Twój adres mailowy. Prosimy o wejście w poniższy link w celu dokończenia procesu rejestracji:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne.";
        String buttonLabel = "Potwierdzenie rejestracji";
        String subject = "Too Good To Go - ponowne wysłanie linku aktywacyjnego";

        try {
            sendEmail(recipientEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendPasswordRecoveryEmail(String recipientEmail) {

        Optional<User> optionalUser = userRepository.findByEmail(recipientEmail);

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User with given e-mail address not found");
        }

        User user = optionalUser.get();
        String url = envConfig.getWebsiteUrl() +
                "/retrievePassword?userId=" + user.getId() + "&token=" + user
                .getPassRecoveryToken();

        String firstParagraph = "Otrzymałeś tę wiadomość, ponieważ podczas prośby o odzyskanie hasła został podany Twój adres mailowy. Kliknij w poniższy link, aby odzyskać hasło:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne.";
        String buttonLabel = "Odzyskaj hasło";
        String subject = "Too Good To Go - przypomnienie hasła";

        try {
            sendEmail(recipientEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void sendPasswordChangeEmail(String recipientEmail) {

        String firstParagraph = "Otrzymałeś tę wiadomość, ponieważ dokonano zmiany hasła do konta przypisanego do Twojego adresu mailowego. Kliknij w poniższy link, aby przejść do panelu logowania:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne. Jeśli to nie Ty dokonałeś zmiany hasła, jak najszybciej zabezpiecz swoje dane i skontaktuj się z Administracją.";
        String buttonLabel = "Logowanie";
        String subject = "Too Good To Go - hasło zostało zmienione";

        String url = envConfig.getWebsiteUrl() + "/auth";

        try {
            sendEmail(recipientEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void sendEmail(String email, String firstParagraph, String secondParagraph, String url, String buttonLabel,
            String subject)
            throws FileNotFoundException {

        File text = new File(envConfig.getEmailMessageFilePath());

        String emailHtml;

        try (Scanner sc = new Scanner(text)) {
            StringBuilder emailHtmlBuilder = new StringBuilder();
            while (sc.hasNextLine()) {
                emailHtmlBuilder.append(sc.nextLine()).append("\n");
            }
            emailHtml = emailHtmlBuilder.toString();
        }

        emailHtml = emailHtml
                .replace("{email}", email)
                .replace("{buttonLabel}", buttonLabel)
                .replace("{firstParagraph}", firstParagraph)
                .replace("{secondParagraph}", secondParagraph)
                .replace("{buttonUrl}", url)
                .replace("{websiteUrl}", envConfig.getWebsiteUrl());

        mailer.send(email, subject, emailHtml);
    }
}
