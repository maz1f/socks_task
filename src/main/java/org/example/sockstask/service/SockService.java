package org.example.sockstask.service;

import org.example.sockstask.dto.SockDto;
import org.example.sockstask.dto.SocksResponse;
import org.example.sockstask.util.Comparison;
import org.example.sockstask.util.FieldForSort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SockService {

    SocksResponse getAllWithFilters(String color, Comparison comp, List<Float> cottonPercentage,
                                    FieldForSort fieldForSort, boolean asc);

    void sockIncome(SockDto sock);

    void sockOutcome(SockDto sock);

    SockDto updateSock(Long id, String color, Float cottonPercentage, Integer quantity);

    void uploadFromFile(MultipartFile file);
}
