package org.example.sockstask.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sockstask.dto.SockDto;
import org.example.sockstask.dto.SocksResponse;
import org.example.sockstask.service.SockService;
import org.example.sockstask.util.Comparison;
import org.example.sockstask.util.FieldForSort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/socks")
@RequiredArgsConstructor
@Tag(name = "socks-service", description = "API для учета носков на складе магазина")
public class SocksController {

    private final SockService sockService;

    @GetMapping
    @Operation(summary = "Получение общего количества носков с фильтрацией")
    public SocksResponse getSocksWithFilter(
            @RequestParam(required = false) String color,
            @RequestParam(required = false, defaultValue = "equal") Comparison comparison,
            @RequestParam(required = false) List<Float> cottonPercentage,
            @RequestParam(required = false, defaultValue = "NOTHING") FieldForSort fieldForSort,
            @RequestParam(required = false, defaultValue = "true") boolean asc) {
        return sockService.getAllWithFilters(color, comparison, cottonPercentage, fieldForSort, asc);
    }

    @PostMapping("/income")
    @Operation(summary = "Регистрация прихода носков")
    public ResponseEntity<?> incomeSocks(@RequestBody @Valid SockDto sock) {
        sockService.sockIncome(sock);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/outcome")
    @Operation(summary = "Регистрация отпуска носков")
    public ResponseEntity<?> outcomeSocks(@RequestBody @Valid SockDto sock) {
        sockService.sockOutcome(sock);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление данных носков")
    public SockDto updateSock(@PathVariable Long id,
                              @RequestParam(required = false) String color,
                              @RequestParam(required = false) Float cottonPercentage,
                              @RequestParam(required = false) Integer quantity) {

        return sockService.updateSock(id, color, cottonPercentage, quantity);
    }

    @PostMapping("/batch")
    @Operation(summary = "Загрузка партий носков из CSV файла")
    public ResponseEntity<?> uploadFromFile(@RequestParam("file") MultipartFile file) {
        sockService.uploadFromFile(file);
        return ResponseEntity.ok().build();
    }

}
