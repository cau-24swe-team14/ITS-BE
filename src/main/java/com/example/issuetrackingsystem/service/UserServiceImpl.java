package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.dto.LoginRequest;
import com.example.issuetrackingsystem.dto.SignUpRequest;
import com.example.issuetrackingsystem.exception.ErrorCode;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.repository.AccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(AccountRepository accountRepository,
        BCryptPasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long login(LoginRequest loginRequest) throws ITSException {
        Account account = accountRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new ITSException(ErrorCode.USERNAME_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            throw new ITSException(ErrorCode.INVALID_PASSWORD);
        }

        return account.getAccountId();
    }

    @Override
    public Long signup(SignUpRequest signUpRequest) throws ITSException {
        if (accountRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            throw new ITSException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

        Account newAccount = accountRepository.save(Account.builder()
            .username(signUpRequest.getUsername())
            .password(encodedPassword)
            .build());

        return newAccount.getAccountId();
    }

    @Override
    public void findUser(String username) {
        if (!accountRepository.findByUsername(username).isPresent())
            throw new ITSException(ErrorCode.USERNAME_NOT_FOUND);
    }
}
