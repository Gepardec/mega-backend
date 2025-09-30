package com.gepardec.mega.domain.mapper;

import java.util.List;

// D: Domain-Object, E: Entity-Object
public interface EntityMapper<D, E> {

    E mapToEntity(D object);

    D mapToDomain(E object);

    default List<E> mapListToEntity(List<D> objects) {
        return objects.stream()
                .map(this::mapToEntity)
                .toList();
    }

    default List<D> mapListToDomain(List<E> objects) {
        return objects.stream()
                .map(this::mapToDomain)
                .toList();
    }
}
