package com.gepardec.mega.rest.mapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class YearMonthParamConverterTest {

    private YearMonthParamConverter converter = new YearMonthParamConverter();

    @Test
    void fromString_ValidInput_YearMonthCreated() {
        YearMonth result = converter.fromString("2023-06");
        assertThat(YearMonth.of(2023, 6)).isEqualTo(result);
    }

    @Test
    void fromString_NullInput_IsNull() {
        YearMonth result = converter.fromString(null);
        assertThat(result).isNull();
    }

    @Test
    void fromString_BlankInput_IsNull() {
        YearMonth result = converter.fromString("  ");
        assertThat(result).isNull();
    }

    @Test
    void fromString_MalformedInput_ThrowsBadRequest() {
        assertThatThrownBy(() -> converter.fromString("2026-13"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("2026-13");
    }

    @Test
    void toString_ValidInput_YearMonthAsString() {
        String result = converter.toString(YearMonth.of(2024, 12));
        assertThat("2024-12").isEqualTo(result);
    }

    @Test
    void toString_NullInput_IsNull() {
        String result = converter.toString(null);
        assertThat(result).isNull();
    }
}
