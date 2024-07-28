package br.com.daniel.userserviceapi.controller.impl;

import br.com.daniel.userserviceapi.entity.User;
import br.com.daniel.userserviceapi.repository.UserRepository;
import br.com.userservice.commonslib.model.requests.CreateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static br.com.daniel.userserviceapi.creator.CreatorUtils.generateMock;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerImplTest {

    public static final String BASE_URI = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByIdWithSuccess() throws Exception {
        var entity = generateMock(User.class);
        entity.setId(null);

        final var userId = userRepository.save(entity).getId();

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entity.getId()))
                .andExpect(jsonPath("$.name").value(entity.getName()))
                .andExpect(jsonPath("$.email").value(entity.getEmail()))
                .andExpect(jsonPath("$.password").value(entity.getPassword()))
                .andExpect(jsonPath("$.profiles").isArray());

        userRepository.deleteById(userId);
    }

    @Test
    void testFindByIdWithNotFound() throws Exception {
        var userId = 1L;

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Object not Found. id" + userId + ", Type: UserResponse"))
                .andExpect(jsonPath("$.error").value(NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.path").value("/api/users/" + userId))
                .andExpect(jsonPath("$.status").value(NOT_FOUND.value()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

    }

    @Test
    void testFindAllWithSuccess() throws Exception {
        var entity = generateMock(User.class);
        entity.setId(null);

        var entity2 = generateMock(User.class);
        entity2.setId(null);

        userRepository.saveAll(List.of(entity, entity2));

        mockMvc.perform(get(BASE_URI))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").isNotEmpty())
                .andExpect(jsonPath("$[1]").isNotEmpty())
                .andExpect(jsonPath("$[0].profiles").isArray())
                .andExpect(jsonPath("$[1].profiles").isArray());

        userRepository.deleteAll(List.of(entity, entity2));

    }

    @Test
    void testSaveWithSuccess() throws Exception {
        final var validEmail = "testeJunit@gmail.com";
        final var request = generateMock(CreateUserRequest.class).withEmail(validEmail);

        mockMvc.perform(post(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON).content(toJson(request))
        ).andExpect(status().isCreated());

        userRepository.deleteByEmail(validEmail);
    }

    @Test
    void testSaveWithConflict() throws Exception {
        final var validEmail = "testeJunit@gmail.com";
        final var entity = generateMock(User.class).withId(null).withEmail(validEmail);

        userRepository.save(entity);

        final var request = generateMock(CreateUserRequest.class).withEmail(validEmail);

        mockMvc.perform(post(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email ["+validEmail+"] already exists."))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value(BASE_URI))
                .andExpect(jsonPath("$.status").value(CONFLICT.value()));


        userRepository.deleteByEmail(validEmail);
    }

    @Test
    void testSaveWithNameEmptyThenThrowBadRequest() throws Exception {
        final var validEmail = "testeJunit@gmail.com";
        final var request = generateMock(CreateUserRequest.class).withEmail(validEmail).withName(null);

        mockMvc.perform(post(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Exception in validation attributes"))
                .andExpect(jsonPath("$.error").value("Validation Exception"))
                .andExpect(jsonPath("$.path").value(BASE_URI))
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.errors[?(@.fieldName==='name' && " +
                        "@.message==='Name cannot be empty')]").exists());

    }


    @Test
    void testSaveWithRequiredLengthThenThrowBadRequest() throws Exception {
        final var validEmail = "testeJunit@gmail.com";
        final var request = generateMock(CreateUserRequest.class).withEmail(validEmail).withName("Ts");

        mockMvc.perform(post(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Exception in validation attributes"))
                .andExpect(jsonPath("$.error").value("Validation Exception"))
                .andExpect(jsonPath("$.path").value(BASE_URI))
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.errors[?(@.fieldName==='name' &&" +
                        " @.message==='Name must contain between 3 and 50 characters')]").exists());
    }

    private String toJson(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (final Exception e){
            throw new RuntimeException(e);
        }
    }
}
