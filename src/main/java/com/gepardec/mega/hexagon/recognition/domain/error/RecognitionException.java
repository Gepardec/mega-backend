package com.gepardec.mega.hexagon.recognition.domain.error;

public abstract class RecognitionException extends RuntimeException {

    protected RecognitionException(String message) {
        super(message);
    }
}
