package com.gepardec.mega.rest.mapper;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
public class YearMonthParamConverterTest {

    private YearMonthParamConverter converter = new YearMonthParamConverter();

    @Test
    public void testFromStringValidInput() {
        YearMonth result = converter.fromString("2023-06");
        assertThat(YearMonth.of(2023, 6)).isEqualTo(result);
    }

    @Test
    public void testFromStringNullInput() {
        YearMonth result = converter.fromString(null);
        assertThat(result).isNull();;
    }

    @Test
    public void testToString() {
        String result = converter.toString(YearMonth.of(2024, 12));
        assertThat("2024-12").isEqualTo(result);
    }

    @Test
    public void testToStringNullInput() {
        String result = converter.toString(null);
        assertThat(result).isNull();
    }

}