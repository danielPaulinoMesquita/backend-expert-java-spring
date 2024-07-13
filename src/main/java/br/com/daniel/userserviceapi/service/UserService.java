package br.com.daniel.userserviceapi.service;

import br.com.daniel.userserviceapi.mapper.UserMapper;
import br.com.daniel.userserviceapi.repository.UserRepository;
import br.com.userservice.commonslib.model.exceptions.ResourceNotFoundException;
import br.com.userservice.commonslib.model.responses.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse findById(String id) {
        return  userMapper.fromEntity(
                userRepository.findById(id)
                              .orElseThrow(() -> new ResourceNotFoundException("Object not Found. id" + id + ", Type: "+UserResponse.class.getSimpleName())));
    }
}
