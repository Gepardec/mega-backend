package com.gepardec.mega.rest.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MapperManager {

    private final ModelMapper modelMapper;

    public MapperManager() {
        this.modelMapper = new ModelMapper();
        this.modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public <T, V> V map(T obj, Class<V> type) {
        return modelMapper.map(obj, type);
    }

    public <T, V> List<V> mapAsList(List<T> objects, Class<V> type) {
        return objects.stream()
                .map(obj -> map(obj, type))
                .collect(Collectors.toList());
    }
}
