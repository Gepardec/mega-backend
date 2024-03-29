package com.gepardec.mega.db.entity.common.converter;

import com.gepardec.mega.db.entity.common.State;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class StateConverter implements AttributeConverter<State, Integer> {

    @Override
    public Integer convertToDatabaseColumn(State state) {
        if (state == null) {
            return null;
        }
        return state.getStateId();
    }

    @Override
    public State convertToEntityAttribute(Integer stateId) {
        if (stateId == null) {
            return null;
        }

        return Stream.of(State.values())
                .filter(s -> s.getStateId() == stateId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
