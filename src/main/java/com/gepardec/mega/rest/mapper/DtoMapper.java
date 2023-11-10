package com.gepardec.mega.rest.mapper;

import java.util.List;
import java.util.stream.Collectors;

public interface DtoMapper<T, U> {

    U mapToDto(T object);

    T mapToDomain(U object);

    default List<U> mapListToDto(List<T> objects) {
        return objects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    default List<T> mapListToDomain(List<U> objects) {
        return objects.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }
}
