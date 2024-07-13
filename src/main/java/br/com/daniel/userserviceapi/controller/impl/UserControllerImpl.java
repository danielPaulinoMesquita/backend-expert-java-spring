package br.com.daniel.userserviceapi.controller.impl;

import br.com.daniel.userserviceapi.controller.UserController;
import br.com.daniel.userserviceapi.service.UserService;
import br.com.userservice.commonslib.model.responses.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> findById(String id) {
        return ResponseEntity.ok().body(userService.findById(id));
    }
}
