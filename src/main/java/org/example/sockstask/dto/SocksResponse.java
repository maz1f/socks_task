package org.example.sockstask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Список носков подходящих по условиям")
public record SocksResponse(
        List<SockDto> socks
) {
}
