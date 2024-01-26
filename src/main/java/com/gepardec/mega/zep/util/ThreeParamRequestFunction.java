package com.gepardec.mega.zep.util;

import jakarta.ws.rs.core.Response;

@FunctionalInterface
public interface ThreeParamRequestFunction<A, B, C> {
    public Response apply(A a, B b, C c, int page);
}



