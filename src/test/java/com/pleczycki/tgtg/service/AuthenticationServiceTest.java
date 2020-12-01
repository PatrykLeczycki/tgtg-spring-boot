package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.dto.UserDto;
import com.pleczycki.tgtg.model.Role;
import com.pleczycki.tgtg.model.RoleName;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.RoleRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.utils.ApiResponse;
import com.pleczycki.tgtg.utils.CustomModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CustomModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private static UserDto userDto;
    private static User user;
    private static User userFromDto;
    private static Role userRole;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        userRole = initializeUserRole();
        userDto = initializeUserDto();
        user = initializeUser();
        userFromDto = initializeUserFromDto();
    }

    private static User initializeUser() {
        final User user = new User();

        user.setId(1L);
        user.setUsername("");
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setLocationsBlacklist(new LinkedList<>());
        user.setRoles(Set.of(userRole));
        user.setEnabled(false);
        user.setRegistrationToken("registrationToken");
        user.setPassRecoveryToken("passRecoveryToken");
        user.setReviews(new ArrayList<>());
        user.setCreatedAt(new Date());

        return user;
    }

    private static UserDto initializeUserDto() {
        final UserDto userDto = new UserDto();

        userDto.setEmail("email@email.com");
        userDto.setPassword("password");

        return userDto;
    }

    private static User initializeUserFromDto() {
        final User user = new User();

        user.setId(1L);
        user.setUsername(null);
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setLocationsBlacklist(new LinkedList<>());
        user.setRoles(new HashSet<>());
        user.setEnabled(false);
        user.setRegistrationToken(null);
        user.setPassRecoveryToken(null);
        user.setReviews(new ArrayList<>());
        user.setCreatedAt(null);

        return user;
    }

    private Role initializeUserRole() {
        final Role role = new Role();

        role.setId(1L);
        role.setName(RoleName.ROLE_USER);

        return role;
    }

    @Test
    void shouldRegisterNewUser() {
        //given
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(modelMapper.map(userDto, User.class)).thenReturn(userFromDto);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));

        //when
        ResponseEntity<ApiResponse> userResponseEntity = authenticationService.register(userDto);

        //then
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertAll(() -> {
            User savedUser = userCaptor.getAllValues().get(0);
            assertNotNull(savedUser);
            assertThat(savedUser.getUsername(), is(""));
            assertThat(savedUser.getRoles(), hasSize(1));
            assertThat(savedUser.getRoles(), contains(userRole));
            assertThat(savedUser.getEmail(), is(userDto.getEmail()));
            assertThat(savedUser.getRegistrationToken(), not(emptyString()));
            assertThat(savedUser.getRegistrationToken().length(), is(30));
            assertFalse(savedUser.isEnabled());
            assertNotNull(userResponseEntity.getBody());
            assertTrue(userResponseEntity.getBody().getSuccess());
            assertThat(userResponseEntity.getBody().getMessage(), is("User registered successfully"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfRegistrationEmailAlreadyExists() {
        //given
        when(userRepository.existsByEmail(any())).thenReturn(true);

        //when
        ResponseEntity<ApiResponse> registrationResponseEntity = authenticationService.register(userDto);

        //then
        assertAll(() -> {
            assertThat(registrationResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(registrationResponseEntity.getBody());
            assertFalse(registrationResponseEntity.getBody().getSuccess());
            assertThat(registrationResponseEntity.getBody().getMessage(), is("Email is already used!"));
        });
    }

    @Test
    void shouldThrowExceptionIfUserRoleNotFound() {
        //given
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(modelMapper.map(userDto, User.class)).thenReturn(userFromDto);
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(RuntimeException.class, () -> authenticationService.register(userDto));
    }

    @Test
    void shouldConfirmAccountIfConfirmationLinkIsValid() {
        //given
        Map<String, String> confirmationData = new HashMap<>();
        confirmationData.put("userId", "1");
        confirmationData.put("registrationToken", "registrationToken");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //when
        ResponseEntity<ApiResponse> confirmAccountResponseEntity = authenticationService
                .confirmAccount(confirmationData);

        //then
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertAll(() -> {
            User savedUser = userCaptor.getAllValues().get(0);
            assertNotNull(savedUser);
            assertThat(savedUser.getUsername(), is(user.getUsername()));
            assertThat(savedUser.getRoles(), is(user.getRoles()));
            assertThat(savedUser.getEmail(), is(user.getEmail()));
            assertNull(savedUser.getRegistrationToken());
            assertTrue(savedUser.isEnabled());
            assertThat(confirmAccountResponseEntity.getStatusCode(), is(HttpStatus.OK));
            assertNotNull(confirmAccountResponseEntity.getBody());
            assertTrue(confirmAccountResponseEntity.getBody().getSuccess());
            assertThat(confirmAccountResponseEntity.getBody().getMessage(), is("Account activated successfully"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfUserIdFromConfirmationLinkIsInvalid() {
        //given
        Map<String, String> confirmationData = new HashMap<>();
        confirmationData.put("userId", "1");
        confirmationData.put("registrationToken", "registrationToken");
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.confirmAccount(confirmationData);

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(confirmAccount.getBody());
            assertFalse(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(), is("E-mail not found"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfConfirmationTokenIsMismatched() {
        //given
        Map<String, String> confirmationData = new HashMap<>();
        confirmationData.put("userId", "1");
        confirmationData.put("registrationToken", "wrongRegistrationToken");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.confirmAccount(confirmationData);

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(confirmAccount.getBody());
            assertFalse(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(), is("Invalid confirmation token"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfEmailFromConfirmationLinkResendingRequestIsInvalid() {
        //given
        Map<String, String> confirmationData = new HashMap<>();
        confirmationData.put("userId", "1");
        confirmationData.put("email", "email@email.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.resendConfirmationLink(confirmationData);

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(confirmAccount.getBody());
            assertFalse(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(), is("E-mail not found"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfEmailFromConfirmationLinkResendingRequestIsAlreadyConfirmed() {
        //given
        Map<String, String> confirmationData = new HashMap<>();
        confirmationData.put("userId", "1");
        confirmationData.put("email", "email@email.com");
        user.setEnabled(true);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.resendConfirmationLink(confirmationData);

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(confirmAccount.getBody());
            assertFalse(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(), is("User is already activated"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfEmailFromPasswordRetrievalRequestIsInvalid() {
        //given
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.retrievePassword(anyString());

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(confirmAccount.getBody());
            assertFalse(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(), is("E-mail not found"));
        });
    }

    @Test
    void shouldRespondWithOkStatusIfEmailFromPasswordRetrievalRequestIsValid() {
        //given
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.retrievePassword(anyString());

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.OK));
            assertNotNull(confirmAccount.getBody());
            assertTrue(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(),
                    is("Message with password retrieve link sent successfully"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfUserIdFromPasswordRetrievalTokenIsInvalid() {
        //given
        Map<String, String> passwordRetrievalData = new HashMap<>();
        passwordRetrievalData.put("userId", "1");
        passwordRetrievalData.put("lostPasswordToken", "wrongLostPasswordToken");
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.retrievePassword(passwordRetrievalData);

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(confirmAccount.getBody());
            assertFalse(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(), is("Invalid password recovery token - user not found."));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfPasswordRetrievalTokenIsMismatched() {
        //given
        Map<String, String> passwordRetrievalData = new HashMap<>();
        passwordRetrievalData.put("userId", "1");
        passwordRetrievalData.put("lostPasswordToken", "wrongLostPasswordToken");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //when
        ResponseEntity<ApiResponse> confirmAccount = authenticationService.retrievePassword(passwordRetrievalData);

        //then
        assertAll(() -> {
            assertThat(confirmAccount.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(confirmAccount.getBody());
            assertFalse(confirmAccount.getBody().getSuccess());
            assertThat(confirmAccount.getBody().getMessage(), is("Invalid password recovery token"));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfIncorrectUserIdIsPassedWithPasswordChangeRequest() {
        //given
        Map<String, String> changePasswordData = new HashMap<>();
        changePasswordData.put("userId", "1");
        changePasswordData.put("currentPassword", "currentPassword");
        changePasswordData.put("newPassword", "newPassword");
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //when
        ResponseEntity<ApiResponse> changePasswordResponseEntity = authenticationService
                .changePassword(changePasswordData);

        //then
        assertAll(() -> {
            assertThat(changePasswordResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(changePasswordResponseEntity.getBody());
            assertFalse(changePasswordResponseEntity.getBody().getSuccess());
            assertThat(changePasswordResponseEntity.getBody().getMessage(), is("User not found."));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusIfPasswordsPassedWithPasswordChangeRequestMismatch() {
        //given
        Map<String, String> changePasswordData = new HashMap<>();
        changePasswordData.put("userId", "1");
        changePasswordData.put("currentPassword", "currentPassword");
        changePasswordData.put("newPassword", "newPassword");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        //when
        ResponseEntity<ApiResponse> changePasswordResponseEntity = authenticationService
                .changePassword(changePasswordData);

        //then
        assertAll(() -> {
            assertThat(changePasswordResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(changePasswordResponseEntity.getBody());
            assertFalse(changePasswordResponseEntity.getBody().getSuccess());
            assertThat(changePasswordResponseEntity.getBody().getMessage(), is("Current password is incorrect"));
        });
    }

    @Test
    void shouldNotSendEmailIfUserIsInvalid() {
        //given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        //when
        //then
        assertAll(() -> {
            assertThrows(ResourceNotFoundException.class, () -> {
                authenticationService.sendPasswordRecoveryEmail("test@email.com");
            });
            assertThrows(ResourceNotFoundException.class, () -> {
                authenticationService.sendRegistrationEmail("test@email.com");
            });
            assertThrows(ResourceNotFoundException.class, () -> {
                authenticationService.resendConfirmationLinkEmail("test@email.com");
            });

        });
    }
}