package com.gepardec.mega.hexagon.shared.domain.error;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainExceptionTest {

    @Test
    void constructor_shouldExposeCodeCategoryAndMessage() {
        // When
        DomainException exception = new SampleException("sample failed");

        // Then
        assertThat(exception.code()).isEqualTo("SAMPLE_FAILED");
        assertThat(exception.category()).isEqualTo(ErrorCategory.INVALID);
        assertThat(exception).hasMessage("sample failed");
    }

    @Test
    void constructor_shouldKeepCause_whenCauseGiven() {
        // Given
        IllegalStateException cause = new IllegalStateException("root");

        // When
        DomainException exception = new SampleException("sample failed", cause);

        // Then
        assertThat(exception.code()).isEqualTo("SAMPLE_FAILED");
        assertThat(exception.category()).isEqualTo(ErrorCategory.INVALID);
        assertThat(exception).hasCause(cause);
    }

    @Test
    void constructor_shouldRejectMissingErrorCode() {
        assertThatThrownBy(() -> new DomainException(null, "message") {
        })
                .isInstanceOf(NullPointerException.class)
                .hasMessage("errorCode must not be null");
    }

    private static final class SampleException extends DomainException {

        SampleException(String message) {
            super(SampleErrorCode.FAILED, message);
        }

        SampleException(String message, Throwable cause) {
            super(SampleErrorCode.FAILED, message, cause);
        }
    }

    private enum SampleErrorCode implements ErrorCode {
        FAILED;

        @Override
        public String boundedContext() {
            return "SAMPLE";
        }

        @Override
        public ErrorCategory category() {
            return ErrorCategory.INVALID;
        }
    }
}
