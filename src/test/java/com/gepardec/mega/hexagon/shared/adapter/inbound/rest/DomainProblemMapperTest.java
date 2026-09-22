package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DomainProblemMapperTest {

    @Mock
    private PostProcessorsRegistry postProcessorsRegistry;

    @ParameterizedTest
    @CsvSource({
            "NOT_FOUND, 404, Not Found",
            "FORBIDDEN, 403, Forbidden",
            "INVALID,   400, Bad Request"
    })
    void toProblem_shouldMapCategoryToStatusWithDetailAndCode(ErrorCategory category, int status, String title) {
        // Given
        DomainProblemMapper mapper = new DomainProblemMapper(postProcessorsRegistry);
        DomainException exception = new SampleException(category, "sample failed");

        // When
        HttpProblem problem = mapper.toProblem(exception);

        // Then
        assertThat(problem.getStatusCode()).isEqualTo(status);
        assertThat(problem.getTitle()).isEqualTo(title);
        assertThat(problem.getDetail()).isEqualTo("sample failed");
        assertThat(problem.getParameters()).containsEntry("code", "SAMPLE_FAILED");
        assertThat(problem.getType()).isNull();
    }

    @Test
    void toProblem_shouldOmitDetail_whenExceptionHasNoMessage() {
        // Given
        DomainProblemMapper mapper = new DomainProblemMapper(postProcessorsRegistry);

        // When
        HttpProblem problem = mapper.toProblem(new SampleException(ErrorCategory.FORBIDDEN, null));

        // Then
        assertThat(problem.getDetail()).isNull();
        assertThat(problem.getParameters()).containsEntry("code", "SAMPLE_FAILED");
    }

    private static final class SampleException extends DomainException {

        SampleException(ErrorCategory category, String message) {
            super(new SampleErrorCode(category), message);
        }
    }

    private record SampleErrorCode(ErrorCategory category) implements ErrorCode {

        @Override
        public String name() {
            return "FAILED";
        }

        @Override
        public String boundedContext() {
            return "SAMPLE";
        }
    }
}
