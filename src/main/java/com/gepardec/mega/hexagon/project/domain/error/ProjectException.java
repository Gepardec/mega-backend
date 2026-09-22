package com.gepardec.mega.hexagon.project.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;

public class ProjectException extends DomainException {

    public ProjectException(ProjectErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ProjectException(ProjectErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
