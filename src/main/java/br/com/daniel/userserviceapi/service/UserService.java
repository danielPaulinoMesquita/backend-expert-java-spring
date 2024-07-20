package br.com.daniel.userserviceapi.service;

import br.com.daniel.userserviceapi.entity.User;
import br.com.daniel.userserviceapi.mapper.UserMapper;
import br.com.daniel.userserviceapi.repository.UserRepository;
import br.com.userservice.commonslib.model.exceptions.ResourceNotFoundException;
import br.com.userservice.commonslib.model.requests.CreateUserRequest;
import br.com.userservice.commonslib.model.requests.UpdateUserRequest;
import br.com.userservice.commonslib.model.responses.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder;

    public UserResponse findById(String id) {
        return  userMapper.fromEntity(find(id));
    }

    public void save(CreateUserRequest createUserRequest) {
        verifyIfEmailAlreadyExists(createUserRequest.email(), null);
        userRepository
                .save(userMapper.fromRequest(createUserRequest)
                        .withPassword(encoder.encode(createUserRequest.password())));
    }

    public UserResponse update(String id, UpdateUserRequest updateUserRequest) {
        User user = find(id);
        verifyIfEmailAlreadyExists(updateUserRequest.email(), id);
        return userMapper.fromEntity(userRepository.save(
                userMapper.update(updateUserRequest, user).withPassword(updateUserRequest.password() != null ?
                        encoder.encode(updateUserRequest.password()) : user.getPassword())
        ));
    }

    private void verifyIfEmailAlreadyExists(final String email, final String id) {
        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new DataIntegrityViolationException("Email ["+email+"] already exists.");
                });
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::fromEntity)
                .toList();
    }

    private User find(String id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object not Found. id" + id + ", Type: " +UserResponse.class.getSimpleName()));
    }

}
