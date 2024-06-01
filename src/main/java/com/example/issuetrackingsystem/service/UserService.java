package com.example.issuetrackingsystem.service;

import com.example.issuetrackingsystem.dto.LoginRequest;
import com.example.issuetrackingsystem.dto.SignUpRequest;

public interface UserService {
  Long login(LoginRequest loginRequest);
  Long signUp(SignUpRequest signUpRequest);
  void findUser(String username);
}
