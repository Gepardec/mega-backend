package com.gepardec.mega.hexagon.shared.domain.error;

/**
 * A stable, transport-neutral identifier of a failure reason. Implemented by one enum per bounded context.
 * The wire code is derived as {@code <boundedContext>_<name>}, so renaming a constant changes the code.
 */
public interface ErrorCode {

    String name();

    String boundedContext();

    ErrorCategory category();

    default String code() {
        return boundedContext() + "_" + name();
    }
}
