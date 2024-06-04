package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.dto.LoginRequest;
import com.example.issuetrackingsystem.dto.SignUpRequest;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testLoginSuccess() throws ITSException {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        Account account = Account.builder()
            .accountId(2L)
            .username("testuser")
            .password("encodedPassword")
            .build();

        when(accountRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())).thenReturn(true);

        Long accountId = userService.login(loginRequest);
        assertEquals(2L, accountId);
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(accountRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        ITSException exception = assertThrows(ITSException.class, () -> userService.login(loginRequest));

        assertEquals(ErrorCode.USERNAME_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testLoginInvalidPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        Account account = Account.builder()
            .accountId(2L)
            .username("testuser")
            .password("encodedPassword")
            .build();

        when(accountRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())).thenReturn(false);

        ITSException exception = assertThrows(ITSException.class, () -> userService.login(loginRequest));

        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
    }

    @Test
    void testSignUpSuccess() throws ITSException {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("password");

        Account account = Account.builder()
            .accountId(3L)
            .username("newuser")
            .password("encodedPassword")
            .build();

        when(accountRepository.findByUsername(signUpRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Long accountId = userService.signup(signUpRequest);
        assertEquals(3L, accountId);
    }

    @Test
    void testSignUpUsernameAlreadyExists() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("password");

        Account account = Account.builder()
            .accountId(3L)
            .username("newuser")
            .password("encodedPassword")
            .build();

        when(accountRepository.findByUsername(signUpRequest.getUsername())).thenReturn(Optional.of(account));

        ITSException exception = assertThrows(ITSException.class, () -> userService.signup(signUpRequest));

        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void testFindUserSuccess() throws ITSException {
        String username = "testuser";

        Account account = Account.builder()
            .accountId(2L)
            .username("testuser")
            .password("encodedPassword")
            .build();

        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        userService.findUser(username);
    }

    @Test
    void testFindUserNotFound() {
        String username = "testuser";

        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());

        ITSException exception = assertThrows(ITSException.class, () -> userService.findUser(username));

        assertEquals(ErrorCode.USERNAME_NOT_FOUND, exception.getErrorCode());
    }
}
