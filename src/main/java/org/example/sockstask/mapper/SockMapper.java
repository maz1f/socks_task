package org.example.sockstask.mapper;

import org.example.sockstask.dto.SockDto;
import org.example.sockstask.entity.Sock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface SockMapper {

    SockDto toSockDto(Sock sock);

    @Mapping(expression = "java(null)", target = "id")
    Sock toSock(SockDto sockDto);
}
