package com.gepardec.mega.zep.rest.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Mapper <D, T>{

    D map (T t);

    default List<D> mapList(List<T> tList) {
        if (tList == null)
            return null;

        return tList.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .toList();
    }
}
