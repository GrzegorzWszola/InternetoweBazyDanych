package com.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.api.controllers.UsersApi;
import com.backend.api.model.ChangeClearanceRequest;
import com.backend.api.model.ChangeRoleRequest;
import com.backend.api.model.UserPage;
import com.backend.api.model.UserResponse;
import com.backend.services.UserService;

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
    public ResponseEntity<UserResponse> changeUserClearance(Integer id,
            @Valid ChangeClearanceRequest changeClearanceRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'changeUserClearance'");
    }

    @Override
    public ResponseEntity<UserResponse> changeUserRole(Integer id, @Valid ChangeRoleRequest changeRoleRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'changeUserRole'");
    }

    @Override
    public ResponseEntity<UserResponse> getUser(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public ResponseEntity<UserPage> listUsers(@Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer size) {
        throw new UnsupportedOperationException("Unimplemented method 'listUsers'");
    }
}
