package com.gepardec.mega.rest.mapper;

import java.util.List;
import java.util.stream.Collectors;

abstract class DtoMapper<T, U> {

    public abstract U mapToDto(T object);

    public abstract T mapToDomain(U object);

    public List<U> mapListToDto(List<T> objects) {
        return objects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<T> mapListToDomain(List<U> objects) {
        return objects.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }
}
