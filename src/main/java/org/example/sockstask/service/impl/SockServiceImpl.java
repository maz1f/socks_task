package org.example.sockstask.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.sockstask.dto.SockDto;
import org.example.sockstask.dto.SocksResponse;
import org.example.sockstask.entity.Sock;
import org.example.sockstask.exception.*;
import org.example.sockstask.mapper.SockMapper;
import org.example.sockstask.repository.SockRepository;
import org.example.sockstask.service.SockService;
import org.example.sockstask.util.Comparison;
import org.example.sockstask.util.FieldForSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SockServiceImpl implements SockService {

    private final SockRepository sockRepository;
    private final SockMapper sockMapper;

    @Override
    @Transactional(readOnly = true)
    public SocksResponse getAllWithFilters(String color, Comparison comp, List<Float> cottonPercentage,
                                           FieldForSort fieldForSort, boolean asc) {

        if (cottonPercentage != null) {
            if ((cottonPercentage.size() == 1 && comp.equals(Comparison.between)) ||
                    (cottonPercentage.size() == 2 && !comp.equals(Comparison.between)) ||
                    cottonPercentage.size() > 2 )
                throw new InvalidParametersException();

            if (cottonPercentage.size() == 2)
                cottonPercentage.sort(Comparator.naturalOrder());
        }

        List<Sock> socks = sockRepository.findAllWithFilters(color, comp, cottonPercentage);

        if (socks.isEmpty())
            throw new SockNotFoundException();

        Comparator<Sock> comparator = switch (fieldForSort) {
            case COLOR -> Comparator.comparing(Sock::getColor);
            case COTTON_PERCENTAGE -> Comparator.comparing(Sock::getCottonPercentage);
            case NOTHING -> Comparator.comparing(Sock::getId);
        };

        if (!asc)
            comparator = comparator.reversed();

        socks.sort(comparator);

        return SocksResponse.builder()
                .socks(socks.stream().map(sockMapper::toSockDto).toList())
                .build();
    }

    @Override
    @Transactional
    public void sockIncome(SockDto sock) {
        sockRepository.findByColorAndCottonPercentage(sock.color(), sock.cottonPercentage())
                .ifPresentOrElse(
                        s -> s.income(sock.quantity()),
                        () -> sockRepository.save(sockMapper.toSock(sock))
                );
    }

    @Override
    @Transactional
    public void sockOutcome(SockDto sock) {
        Sock socks = sockRepository.findByColorAndCottonPercentage(sock.color(), sock.cottonPercentage())
                .orElseThrow(SockNotFoundException::new);

        if (socks.getQuantity() < sock.quantity())
            throw new NotEnoughSocksException();

        socks.outcome(sock.quantity());
    }

    @Override
    @Transactional
    public SockDto updateSock(Long id, String color, Float cottonPercentage, Integer quantity) {
        Sock sock = sockRepository.findById(id).orElseThrow(SockNotFoundException::new);
        if (color != null)
            sock.setColor(color);
        if (cottonPercentage != null)
            sock.setCottonPercentage(cottonPercentage);
        if (quantity != null)
            sock.setQuantity(quantity);
        return sockMapper.toSockDto(sock);
    }

    @Override
    @Transactional
    public void uploadFromFile(MultipartFile file) {
        List<Sock> socks = new ArrayList<>();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setDelimiter(',')
                .setHeader()
                .build();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {
            for (CSVRecord csvRecord : csvParser) {
                Sock sock = new Sock();
                sock.setColor(csvRecord.get("color"));
                sock.setCottonPercentage(Float.parseFloat(csvRecord.get("cottonPercentage")));
                sock.setQuantity(Integer.parseInt(csvRecord.get("quantity")));

                sockRepository.findByColorAndCottonPercentage(sock.getColor(), sock.getCottonPercentage())
                        .ifPresentOrElse(
                                s -> s.income(sock.getQuantity()),
                                () -> socks.add(sock)
                        );
            }

        } catch (IOException | IllegalArgumentException e) {
            throw new InvalidCsvFileException();
        }

        sockRepository.saveAll(socks);
    }
}
