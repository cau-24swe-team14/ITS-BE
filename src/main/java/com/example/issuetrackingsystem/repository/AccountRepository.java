package com.example.issuetrackingsystem.repository;

import com.example.issuetrackingsystem.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
