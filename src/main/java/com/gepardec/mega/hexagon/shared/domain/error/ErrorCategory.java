package com.gepardec.mega.hexagon.shared.domain.error;

/**
 * Transport-neutral kinds of rejection. Inbound adapters decide how each is expressed in a protocol.
 */
public enum ErrorCategory {
    /** The object the request addresses doesn't exist. */
    NOT_FOUND,
    /** The acting user may not perform the operation. */
    FORBIDDEN,
    /** The current state of the object the request addresses doesn't allow the operation. */
    CONFLICT,
    /** The request is well-formed, but what it refers to or asks for doesn't fit the domain. */
    UNPROCESSABLE,
    /** The request content is malformed. */
    INVALID
}
