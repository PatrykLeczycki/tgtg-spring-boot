package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.config.AppConfig;
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

@Service("AuthenticationService")
public class AuthenticationService {

    @Autowired
    private AppConfig appConfig;

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
    public ResponseEntity<?> register(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email is already used!"),
                    HttpStatus.BAD_REQUEST);
        } else {
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

    public ResponseEntity<?> confirmAccount(Map<String, String> confirmationData) {
        Long id = Long.valueOf(confirmationData.get("userId"));
        String token = confirmationData.get("registrationToken");
        User user = userRepository.getOne(id);

        if (user.getRegistrationToken().equals(token)) {
            user.setRegistrationToken(null);
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            return new ResponseEntity(new ApiResponse(false, "Invalid confirmation token"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(new ApiResponse(true, "Account activated successfully"), HttpStatus.OK);
    }

    public ResponseEntity<?> resendConfirmationLink(Map<String, String> resendConfirmationLinkData) {
        String email = resendConfirmationLinkData.get("email");
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return new ResponseEntity(new ApiResponse(false, "E-mail not found"), HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        if (!user.isEnabled()) {
            user.setRegistrationToken(RandomStringUtils.randomAlphanumeric(30));
            User save = userRepository.save(user);
            new Thread(() -> resendConfirmationLinkEmail(save.getEmail())).start();
            return new ResponseEntity(new ApiResponse(true, "Confirmation link resent successfully"), HttpStatus.OK);
        }
        return new ResponseEntity(new ApiResponse(false, "User is already activated"), HttpStatus.BAD_REQUEST);
    }

    @Transactional
    public ResponseEntity<?> retrievePassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return new ResponseEntity(new ApiResponse(false, "E-mail not found"), HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();
        user.setPassRecoveryToken(RandomStringUtils.randomAlphanumeric(30));
        return new ResponseEntity(new ApiResponse(true, "Message with password retrieve link sent successfully"),
                HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> retrievePassword(Map<String, String> passwordRetrievalData) {

        User user = userRepository.getOne(Long.valueOf(passwordRetrievalData.get("userId")));
        String token = passwordRetrievalData.get("lostPasswordToken");

        if (!Objects.isNull(user) && user.getPassRecoveryToken().equals(token)) {
            user.setPassword(passwordEncoder.encode(passwordRetrievalData.get("password")));
            user.setPassRecoveryToken(null);
            new Thread(() -> sendPasswordChangeEmail(user.getEmail())).start();
            userRepository.save(user);
        } else {
            return new ResponseEntity(new ApiResponse(false, "Invalid password recovery token"),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(new ApiResponse(true, "Password changed successfully"), HttpStatus.OK);
    }

    public ResponseEntity<?> changePassword(Map<String, String> changePasswordData) {
        Long userId = Long.valueOf(changePasswordData.get("userId"));
        String currentPassword = changePasswordData.get("currentPassword");
        String newPassword = changePasswordData.get("newPassword");

        User user = userRepository.getOne(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return new ResponseEntity(new ApiResponse(false, "Current password is incorrect"), HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        new Thread(() -> sendPasswordChangeEmail(user.getEmail())).start();
        return new ResponseEntity(new ApiResponse(true, "Password changed successfully"), HttpStatus.OK);
    }

    public void sendRegistrationEmail(String receiverEmail) {

        Optional<User> optionalUser = userRepository.findByEmail(receiverEmail);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        String url = appConfig.getWebsiteUrl() +
                "/confirmAccount?userId=" + user.getId() + "&token=" + user.getRegistrationToken();
        String firstParagraph = "Dziękujemy za zarejestrowanie się na naszej stronie. Prosimy o wejście w poniższy link w celu dokończenia procesu rejestracji:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne.";
        String buttonLabel = "Potwierdzenie rejestracji";
        String subject = "Rejestracja w Too Good To Go";

        try {
            sendEmail(receiverEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void resendConfirmationLinkEmail(String receiverEmail) {

        Optional<User> optionalUser = userRepository.findByEmail(receiverEmail);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        String url = appConfig.getWebsiteUrl() +
                "/confirmAccount?userId=" + user.getId() + "&token=" + user.getRegistrationToken();
        String firstParagraph = "Otrzymałeś tę wiadomość, ponieważ podczas prośby o ponowne wysłanie linku aktywacyjnego został podany Twój adres mailowy. Prosimy o wejście w poniższy link w celu dokończenia procesu rejestracji:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne.";
        String buttonLabel = "Potwierdzenie rejestracji";
        String subject = "Too Good To Go - ponowne wysłanie linku aktywacyjnego";

        try {
            sendEmail(receiverEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendPasswordRecoveryEmail(String receiverEmail) {

        Optional<User> optionalUser = userRepository.findByEmail(receiverEmail);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        String url = appConfig.getWebsiteUrl() +
                "/retrievePassword?userId=" + user.getId() + "&token=" + user
                .getPassRecoveryToken();

        String firstParagraph = "Otrzymałeś tę wiadomość, ponieważ podczas prośby o odzyskanie hasła został podany Twój adres mailowy. Kliknij w poniższy link, aby odzyskać hasło:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne.";
        String buttonLabel = "Odzyskaj hasło";
        String subject = "Too Good To Go - przypomnienie hasła";

        try {
            sendEmail(receiverEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendPasswordChangeEmail(String receiverEmail) {

        String firstParagraph = "Otrzymałeś tę wiadomość, ponieważ dokonano zmiany hasła do konta przypisanego do Twojego adresu mailowego. Kliknij w poniższy link, aby przejść do panelu logowania:";
        String secondParagraph = "Jeśli uważasz, że otrzymałeś tę wiadomość omyłkowo, zignoruj ją i upewnij się, że Twoje dane są bezpieczne. Jeśli to nie Ty dokonałeś zmiany hasła, jak najszybciej zabezpiecz swoje dane i skontaktuj się z Administracją.";
        String buttonLabel = "Logowanie";
        String subject = "Too Good To Go - hasło zostało zmienione";

        String url = appConfig.getWebsiteUrl() + "/auth";

        try {
            sendEmail(receiverEmail, firstParagraph, secondParagraph, url, buttonLabel, subject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(String email, String firstParagraph, String secondParagraph, String url, String buttonLabel,
            String subject)
            throws FileNotFoundException {

        File text = new File(appConfig.getEmailMessageFilePath());

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
                .replace("{websiteUrl}", appConfig.getWebsiteUrl());

        mailer.send(email, subject, emailHtml);
    }
}
