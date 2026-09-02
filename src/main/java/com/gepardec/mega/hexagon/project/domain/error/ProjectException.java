package com.gepardec.mega.hexagon.project.domain.error;

public abstract class ProjectException extends RuntimeException {

    protected ProjectException(String message) {
        super(message);
    }

    protected ProjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
