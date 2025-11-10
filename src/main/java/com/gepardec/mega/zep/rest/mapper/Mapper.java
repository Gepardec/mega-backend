package com.gepardec.mega.zep.rest.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface Mapper<D, T> {

    D map(T t);

    default List<D> mapList(List<T> tList) {
        if (tList == null)
            return Collections.emptyList();

        return tList.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .toList();
    }
}
