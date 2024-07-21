package br.com.daniel.userserviceapi.service;

import br.com.daniel.userserviceapi.entity.User;
import br.com.daniel.userserviceapi.mapper.UserMapper;
import br.com.daniel.userserviceapi.repository.UserRepository;
import br.com.userservice.commonslib.model.enums.ProfileEnum;
import br.com.userservice.commonslib.model.exceptions.ResourceNotFoundException;
import br.com.userservice.commonslib.model.requests.CreateUserRequest;
import br.com.userservice.commonslib.model.requests.UpdateUserRequest;
import br.com.userservice.commonslib.model.responses.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static br.com.daniel.userserviceapi.creator.CreatorUtils.generateMock;
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
        when(userMapper.fromEntity(any(User.class))).thenReturn(generateMock(UserResponse.class));

        final UserResponse userResponse = userService.findById(anyString());

        assertNotNull(userResponse);
        assertEquals(UserResponse.class, userResponse.getClass());

        verify(userRepository).findById(anyString());
        verify(userMapper).fromEntity(any(User.class));
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

        verify(userRepository).findById(anyString());
        verify(userMapper, times(0)).fromEntity(any(User.class));
    }

    @Test
    void whenCallFindAllWithValidIdThenReturnListOfUserResponse() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        when(userMapper.fromEntity(any(User.class))).thenReturn(generateMock(UserResponse.class));

        final List<UserResponse> userResponses = userService.findAll();

        assertNotNull(userResponses);
        assertEquals(2, userResponses.size());
        assertEquals(UserResponse.class, userResponses.get(0).getClass());

        verify(userRepository).findAll();
        verify(userMapper, times(2)).fromEntity(any(User.class));
    }

    @Test
    void whenCallSaveThenSuccess() {
        final var request = generateMock(CreateUserRequest.class);

        when(userMapper.fromRequest(any())).thenReturn(new User());
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("password");
        when(userRepository.save(any(User.class))).thenReturn(generateMock(User.class));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        userService.save(request);

        verify(userMapper).fromRequest(request);
        verify(bCryptPasswordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
        verify(userRepository).findByEmail(request.email());

    }

    @Test
    void whenCallSaveWithInvalidEmailThenThrowDataIntegrityViolationException() {
        final var request = generateMock(CreateUserRequest.class);
        final var entity = generateMock(User.class);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(entity));

        try {
            userService.save(request);
        } catch (Exception e) {
            assertEquals(DataIntegrityViolationException.class, e.getClass());
            assertEquals("Email ["+request.email()+"] already exists.", e.getMessage());
        }

        verify(userRepository).findByEmail(request.email());
        verify(userMapper, times(0)).fromRequest(any());
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenCallUpdateThenReturnUserResponse() {
        final var password = "1234";
        final var id = "1";

        final var updateUserRequest = factoryUpdateUserWithPassword(password);
        final var userAlreadySaved = generateMock(User.class);

        when(userRepository.findById(id)).thenReturn(Optional.of(userAlreadySaved));
        when(userRepository.findByEmail(updateUserRequest.email())).thenReturn(Optional.empty());

        when(userMapper.update(updateUserRequest, userAlreadySaved)).thenReturn(generateMock(User.class));
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(password);

        when(userRepository.save(any(User.class))).thenReturn(generateMock(User.class));
        when(userMapper.fromEntity(any(User.class))).thenReturn(generateMock(UserResponse.class));

        final UserResponse userResponse = userService.update(id, updateUserRequest);

        assertNotNull(userResponse);
        assertEquals(UserResponse.class, userResponse.getClass());
        verify(userRepository).findById(id);
        verify(bCryptPasswordEncoder).encode(anyString());
    }


    @Test
    void whenCallUpdateWithDifferentPasswordThenReturnUserResponse() {
        final var updateUserRequest = factoryUpdateUserWithPassword(null);
        final var userAlreadySaved = generateMock(User.class);

        when(userRepository.findById("1")).thenReturn(Optional.of(userAlreadySaved));
        when(userRepository.findByEmail(updateUserRequest.email())).thenReturn(Optional.empty());

        when(userMapper.update(updateUserRequest, userAlreadySaved)).thenReturn(generateMock(User.class));
        when(userMapper.update(updateUserRequest, userAlreadySaved).withPassword(null)).thenReturn(generateMock(User.class));

        when(userRepository.save(any(User.class))).thenReturn(generateMock(User.class));
        when(userMapper.fromEntity(any(User.class))).thenReturn(generateMock(UserResponse.class));

        final UserResponse userResponse = userService.update("1", updateUserRequest);

        assertNotNull(userResponse);
        assertEquals(UserResponse.class, userResponse.getClass());
        verify(userRepository).findById("1");
        verify(bCryptPasswordEncoder, never()).encode(anyString());
    }

    @Test
    void whenCallUpdateWithInvalidIdThenThrowResourceNotFoundException() {
        final var id = "1";
        final var request = generateMock(UpdateUserRequest.class);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        try {
            userService.update(id, request);
        } catch (Exception e) {
            assertEquals(ResourceNotFoundException.class, e.getClass());
            assertEquals("Object not Found. id" + id + ", Type: " +UserResponse.class.getSimpleName(), e.getMessage());
        }

        verify(userRepository).findById(id);
        verify(userRepository, never()).findByEmail(request.email());
        verify(userMapper, never()).fromRequest(any());
        verify(userMapper, never()).update(request, generateMock(User.class));
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenCallUpdateWithInvalidEmailThenThrowDataIntegrityViolationException() {
       final var id = "1";

       final var request = generateMock(UpdateUserRequest.class);
       final var entity = generateMock(User.class);

       when(userRepository.findById(id)).thenReturn(Optional.of(entity));
       when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(entity));

        try {
            userService.update(id, request);
        } catch (Exception e) {
            assertEquals(DataIntegrityViolationException.class, e.getClass());
            assertEquals("Email ["+request.email()+"] already exists.", e.getMessage());
        }

        verify(userRepository).findById(id);
        verify(userRepository).findByEmail(request.email());
        verify(userMapper, times(0)).fromRequest(any());
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    private UpdateUserRequest factoryUpdateUserWithPassword(final String password){
        return new UpdateUserRequest(
                "teste name",
                "teste@gmail.com",
                password,
                Set.of(ProfileEnum.ROLE_ADMIN)
        );
    }

}