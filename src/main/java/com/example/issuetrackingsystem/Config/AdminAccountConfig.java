package com.example.issuetrackingsystem.Config;

import com.example.issuetrackingsystem.domain.Account;
import com.example.issuetrackingsystem.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountConfig {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    public AdminAccountConfig(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdminAccount() {
        if (!accountRepository.findByUsername("admin").isPresent()) {
            Account adminAccount = Account.builder()
                .accountId(0L)
                .username("admin")
                .password(passwordEncoder.encode("1234"))
                .build();
            accountRepository.save(adminAccount);
        }
    }
}
