package org.example.sockstask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import static org.example.sockstask.util.Constant.*;

@Schema(description = "Носок")
public record SockDto(
        @Schema(description = "Цвет носка", example = COLOR_EXAMPLE)
        String color,

        @Schema(description = "Процент содержания хлопка", example = COTTON_PERCENTAGE_EXAMPLE)
        @Positive
        @Min(0) @Max(100)
        float cottonPercentage,

        @Schema(description = "Количество на складе", example = QUANTITY_EXAMPLE)
        @Positive
        int quantity
) {
}
