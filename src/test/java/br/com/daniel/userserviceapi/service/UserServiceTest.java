package br.com.daniel.userserviceapi.service;

import br.com.daniel.userserviceapi.entity.User;
import br.com.daniel.userserviceapi.mapper.UserMapper;
import br.com.daniel.userserviceapi.repository.UserRepository;
import br.com.userservice.commonslib.model.exceptions.ResourceNotFoundException;
import br.com.userservice.commonslib.model.responses.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    void whenCallFindByIdWithValidIdThenReturnUserResponse() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(new User()));
        when(userMapper.fromEntity(any(User.class))).thenReturn(mock(UserResponse.class));

        final UserResponse userResponse = userService.findById(anyString());

        assertNotNull(userResponse);
        assertEquals(UserResponse.class, userResponse.getClass());

        verify(userRepository, times(1)).findById(anyString());
        verify(userMapper, times(1)).fromEntity(any(User.class));
    }

    @Test
    void whenCallFindByIdWithInvalidIdThenReturnNotFoundException() {
        String identify = "1";
        when(userRepository.findById(identify)).thenReturn(Optional.empty());

        try {
            userService.findById(identify);
        } catch (Exception e) {
            assertEquals(ResourceNotFoundException.class, e.getClass());
            assertEquals("Object not Found. id" + identify + ", Type: " +UserResponse.class.getSimpleName(), e.getMessage());
        }

        verify(userRepository, times(1)).findById(anyString());
        verify(userMapper, times(0)).fromEntity(any(User.class));
    }

    @Test
    void whenCallFindAllWithValidIdThenReturnListOfUserResponse() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        when(userMapper.fromEntity(any(User.class))).thenReturn(mock(UserResponse.class));

        final List<UserResponse> userResponses = userService.findAll();

        assertNotNull(userResponses);
        assertEquals(2, userResponses.size());
        assertEquals(UserResponse.class, userResponses.get(0).getClass());

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).fromEntity(any(User.class));
    }

}