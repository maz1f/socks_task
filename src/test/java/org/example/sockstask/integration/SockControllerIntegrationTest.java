package org.example.sockstask.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sockstask.dto.SockDto;
import org.example.sockstask.service.SockService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.stream.Stream;

import static org.example.sockstask.TestData.SOCK_DTO;
import static org.example.sockstask.util.Constant.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
@Transactional
public class SockControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> sqlContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("socks")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final String URL = "http://localhost/api/socks";

    @Autowired
    private SockService sockService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Регистрация прихода носков - добавление к существующим")
    void incomeSocks_addToExist_expect201Status() throws Exception {
        sockService.sockIncome(SOCK_DTO);

        mockMvc.perform(post(URL + "/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SOCK_DTO)))
                .andExpectAll(
                        status().isCreated()
                );
    }

    @Test
    @DisplayName("Регистрация прихода носков - создание новых")
    void incomeSocks_createNew_expect201Status() throws Exception{
        mockMvc.perform(post(URL + "/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SOCK_DTO)))
                .andExpectAll(
                        status().isCreated()
                );
    }

    @Test
    @DisplayName("Регистрация отпуска носков - успех")
    void outcomeSocks_success_expect200() throws Exception{
        sockService.sockIncome(SOCK_DTO);

        SockDto request = new SockDto(SOCK_DTO.color(),
                SOCK_DTO.cottonPercentage(), SOCK_DTO.quantity()-1);

        mockMvc.perform(post(URL + "/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isOk()
                );

    }

    @Test
    @DisplayName("Регистрация отпуска носков - нехватка на складе")
    void outcomeSocks_notEnoughSocks_expect400() throws Exception{
        sockService.sockIncome(SOCK_DTO);
        SockDto request = new SockDto(SOCK_DTO.color(),
                SOCK_DTO.cottonPercentage(), SOCK_DTO.quantity()+1);

        mockMvc.perform(post(URL + "/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isBadRequest()
                );

    }

    @Test
    @DisplayName("Регистрация отпуска носков - таких носков нет")
    void outcomeSocks_sockNotFound_expect404() throws Exception {
        mockMvc.perform(post(URL + "/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SOCK_DTO)))
                .andExpectAll(
                        status().isNotFound()
                );

    }

    @ParameterizedTest
    @DisplayName("Регистрация отпуска носков - не валидные параметры запроса")
    @MethodSource("invalidRequestParamsForOutcomeSocks")
    void outcomeSocks_invalidParameters_expect404(String color, float cottonPercentage, int quantity) throws Exception {
        SockDto request = new SockDto(color, cottonPercentage, quantity);

        mockMvc.perform(post(URL + "/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isBadRequest()
                );
    }

    private static Stream<Arguments> invalidRequestParamsForOutcomeSocks() {
        return Stream.of(
                Arguments.of(COLOR_EXAMPLE, -1F, 5),
                Arguments.of(COLOR_EXAMPLE, 102F, 5),
                Arguments.of(COLOR_EXAMPLE, 90F, -2)
        );
    }
}
