package com.gepardec.mega.service.mapper;

import java.util.List;
import java.util.stream.Collectors;

// D: Domain-Object, E: Entity-Object
public interface DomainMapper<D, E> {

    E mapToEntity(D object);

    D mapToDomain(E object);

    default List<E> mapListToEntity(List<D> objects) {
        return objects.stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());
    }

    default List<D> mapListToDomain(List<E> objects) {
        return objects.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }
}
