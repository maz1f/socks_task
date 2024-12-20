package org.example.sockstask.service;

import org.example.sockstask.entity.Sock;
import org.example.sockstask.exception.InvalidCsvFileException;
import org.example.sockstask.exception.NotEnoughSocksException;
import org.example.sockstask.exception.SockNotFoundException;
import org.example.sockstask.mapper.SockMapper;
import org.example.sockstask.repository.SockRepository;
import org.example.sockstask.service.impl.SockServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.example.sockstask.TestData.*;
import static org.example.sockstask.util.Constant.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SockServiceTest {

    @Mock
    private SockRepository sockRepository;

    @Mock
    private SockMapper sockMapper;

    @InjectMocks
    private SockServiceImpl sockService;

    @Test
    @DisplayName("Добавление количества к уже существующим носкам")
    void sockIncome_expectAddQuantityToExist() {
        Sock mockedSock = mock(Sock.class);
        when(sockRepository.findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage()))
                .thenReturn(Optional.of(mockedSock));
        sockService.sockIncome(SOCK_DTO);

        verify(sockRepository, times(1))
                .findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage());
        verify(mockedSock, times(1)).income(SOCK_DTO.quantity());

    }

    @Test
    @DisplayName("Добавление ранее не существовавших носков")
    void sockIncome_expectCreateNewSock() {
        when(sockRepository.findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage()))
                .thenReturn(Optional.empty());
        when(sockMapper.toSock(SOCK_DTO)).thenReturn(SOCK);

        sockService.sockIncome(SOCK_DTO);

        verify(sockRepository, times(1))
                .findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage());
        verify(sockRepository, times(1)).save(SOCK);
        verify(sockMapper, times(1)).toSock(SOCK_DTO);
    }

    @Test
    @DisplayName("Отгрузка носков - успех")
    void sockOutcome_expectSuccess() {
        Sock mockedSock = mock(Sock.class);
        when(mockedSock.getQuantity()).thenReturn(50);
        when(sockRepository.findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage()))
                .thenReturn(Optional.of(mockedSock));

        sockService.sockOutcome(SOCK_DTO);

        verify(sockRepository, times(1))
                .findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage());
        verify(mockedSock, times(1)).outcome(SOCK_DTO.quantity());

    }


    @Test
    @DisplayName("Отгрузка носков - не хватает носков на складе")
    void sockOutcome_expectNotEnoughSocksException() {
        Sock mockedSock = mock(Sock.class);
        when(mockedSock.getQuantity()).thenReturn(0);
        when(sockRepository.findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage()))
                .thenReturn(Optional.of(mockedSock));


        assertThrows(NotEnoughSocksException.class,
                () -> sockService.sockOutcome(SOCK_DTO));
        verify(sockRepository, times(1))
                .findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage());
        verify(mockedSock, never()).outcome(SOCK_DTO.quantity());
    }


    @Test
    @DisplayName("Отгрузка носков - таких носков нет")
    void sockOutcome_expectSockNotFoundException() {
        when(sockRepository.findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage()))
                .thenReturn(Optional.empty());

        assertThrows(SockNotFoundException.class,
                () -> sockService.sockOutcome(SOCK_DTO));
        verify(sockRepository, times(1))
                .findByColorAndCottonPercentage(SOCK_DTO.color(), SOCK_DTO.cottonPercentage());
    }

    @ParameterizedTest
    @DisplayName("Обновление записи - успех")
    @MethodSource("requestParamsForUpdateSock")
    void updateSock_expectSuccess(String color, Float cottonPercentage, Integer quantity, List<String> fields) {
        Long id = 1L;
        Sock mocked = mock(Sock.class);
        when(sockRepository.findById(id)).thenReturn(Optional.of(mocked));

        sockService.updateSock(id, color, cottonPercentage, quantity);

        for (String field : fields) {
            switch (field) {
                case "color" -> verify(mocked, times(1)).setColor(color);
                case "cottonPercentage" -> verify(mocked, times(1))
                        .setCottonPercentage(cottonPercentage);
                case "quantity" -> verify(mocked, times(1)).setQuantity(quantity);
            }
        }
    }

    @Test
    @DisplayName("Обновление записи - нет такой записи")
    void updateSock_expectSockNotFoundException() {
        Long id = 1L;
        when(sockRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(SockNotFoundException.class,
                () -> sockService.updateSock(id, COLOR_EXAMPLE,
                        Float.parseFloat(COTTON_PERCENTAGE_EXAMPLE), Integer.parseInt(QUANTITY_EXAMPLE))
        );
    }

    @Test
    @DisplayName("Загрузка данных из файла - успех")
    void uploadFromFile_expectSuccess() {
        MockMultipartFile mockFile = new MockMultipartFile("test.csv",
                "color,cottonPercentage,quantity\nwhite,33.5,10\nblack,90,40\n".getBytes());

        when(sockRepository.findByColorAndCottonPercentage(anyString(), anyFloat()))
                .thenReturn(Optional.empty());

        sockService.uploadFromFile(mockFile);

        verify(sockRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Загрузка данных из файла - выброшено исключение")
    void uploadFromFile_expectInvalidCsvFileException() {
        MockMultipartFile mockFile = new MockMultipartFile("test1.csv",
                "color,percentage,quantity\nwhite,33.5,10\nblack,90,40\n".getBytes());

        lenient().when(sockRepository.findByColorAndCottonPercentage(anyString(), anyFloat()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCsvFileException.class,
                () -> sockService.uploadFromFile(mockFile)
        );

    }

    private static Stream<Arguments> requestParamsForUpdateSock() {
        return Stream.of(
                Arguments.of("White", null, null, List.of("color")),
                Arguments.of("White", 99.9f, null, List.of("color", "cottonPercentage")),
                Arguments.of(null, 99.9f, null, List.of("cottonPercentage")),
                Arguments.of(null, 99.9f, 20, List.of("cottonPercentage", "quantity")),
                Arguments.of(null, null, 20, List.of("quantity")),
                Arguments.of("White", null, 20, List.of("color", "quantity")),
                Arguments.of("White", 99.9f, 20, List.of("color", "cottonPercentage", "quantity")),
                Arguments.of(null, null, null, emptyList())
        );
    }

}
