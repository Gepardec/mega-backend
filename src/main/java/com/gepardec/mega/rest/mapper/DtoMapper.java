package com.gepardec.mega.rest.mapper;

import java.util.List;
import java.util.stream.Collectors;

// D: Domain-Object, T: Transfer-Object (DTO)
public interface DtoMapper<D, T> {

    T mapToDto(D object);

    D mapToDomain(T object);

    default List<T> mapListToDto(List<D> objects) {
        return objects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    default List<D> mapListToDomain(List<T> objects) {
        return objects.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }
}
