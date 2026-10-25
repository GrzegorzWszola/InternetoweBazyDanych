package com.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.services.UserService;

import com.backend.api.controllers.UsersApi;
import com.backend.api.model.ChangeRoleRequest;
import com.backend.api.model.UserPage;
import com.backend.api.model.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController 
public class UserController implements UsersApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserResponse> changeUserRole(Integer id, @Valid ChangeRoleRequest changeRoleRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changeUserRole'");
    }

    @Override
    public ResponseEntity<UserPage> listUsers(@Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listUsers'");
    }

}