package com.gepardec.mega.domain.mapper.zep;

public interface ZepMapper<D, Z> {
    D mapToDomain(Z object);
}
