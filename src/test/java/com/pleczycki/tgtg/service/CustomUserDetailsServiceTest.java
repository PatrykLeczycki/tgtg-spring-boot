package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    private static User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        user = initializeUser();
    }

    private static User initializeUser() {
        final User user = new User();

        user.setId(2L);
        user.setEmail("email@email.com");
        user.setPassword("password");
        user.setCreatedAt(new Date());
        user.setUsername(null);
        user.setRegistrationToken(null);
        user.setPassRecoveryToken(null);
        user.setEnabled(true);
        user.setReviews(new ArrayList<>());

        return user;
    }

    @Test
    void shouldFindUserByUsernameOrEmail() {
        //given
        String email = "email@email.com";
        when(userRepository.findByUsernameOrEmail(email, email))
                .thenReturn(Optional.of(user));

        //when
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        //then;
        assertAll(() -> {
            assertNotNull(userDetails);
            assertThat(((UserPrincipal) userDetails).getId(), is(user.getId()));
            assertThat(((UserPrincipal) userDetails).getEmail(), is(email));
            assertThat(userDetails.getPassword(), not(emptyString()));
        });
    }

    @Test
    void shouldThrowExceptionIfUsernameOrEmailNotFound() {
        //given
        String email = "wrong-email@email.com";

        //when
        //then;
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
    }

    @Test
    void shouldFindUserById() {
        //given
        long id = user.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        //when
        UserDetails userDetails = userDetailsService.loadUserById(id);

        //then;
        assertAll(() -> {
            assertNotNull(userDetails);
            assertThat(((UserPrincipal) userDetails).getId(), is(user.getId()));
            assertThat(((UserPrincipal) userDetails).getEmail(), is(user.getEmail()));
            assertThat(userDetails.getPassword(), not(emptyString()));
        });
    }

    @Test
    void shouldThrowExceptionIfIdNotFound() {
        //given
        long id = 1L;

        //when
        //then;
        assertThrows(RuntimeException.class, () -> userDetailsService.loadUserById(id));
    }
}