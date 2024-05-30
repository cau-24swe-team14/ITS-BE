package com.example.issuetrackingsystem.controller;

import com.example.issuetrackingsystem.dto.LoginRequest;
import com.example.issuetrackingsystem.dto.SignUpRequest;
import com.example.issuetrackingsystem.exception.ITSException;
import com.example.issuetrackingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity login(HttpSession session, @RequestBody LoginRequest loginRequest) {
        try {
            Long accountId = userService.login(loginRequest);
            session.setAttribute("id", accountId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (ITSException e) {
            return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(e.getErrorCode().getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }

    @PostMapping("/signup")
    public ResponseEntity signUp(HttpSession session, @RequestBody SignUpRequest signUpRequest) {
        try {
            Long accountId = userService.signUp(signUpRequest);
            session.setAttribute("id", accountId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ITSException e) {
            return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(e.getErrorCode().getMessage());
        }
    }
}
