package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ReceiptServiceTest {

    @InjectMock
    @RestClient
    ZepReceiptRestClient zepReceiptRestClient;

    @InjectMock
    Logger logger;

    @Inject
    ReceiptService receiptService;

    @Test
    void getAllReceiptsInRange_whenReceiptsExist_thenReturnListOfReceipts() {
        // Arrange
        YearMonth payrollMonth = YearMonth.of(2023, 5);
        List<ZepReceipt> receipts = new ArrayList<>();
        receipts.add(
                ZepReceipt.builder()
                        .id(1)
                        .employeeId("emp1")
                        .receiptDate(LocalDate.of(2023, 5, 15))
                        .build()
        );
        receipts.add(
                ZepReceipt.builder()
                        .id(2)
                        .employeeId("emp2")
                        .receiptDate(LocalDate.of(2023, 5, 20))
                        .build()
        );

        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(receipts, new ZepResponse.Links(null, null))));

        // Act
        List<ZepReceipt> result = receiptService.getAllReceiptsInRange(payrollMonth);

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(1);
        assertThat(result.get(1).id()).isEqualTo(2);
    }

    @Test
    void getAllReceiptsInRange_whenExceptionIsThrown_thenLogErrorAndReturnEmptyList() {
        // Arrange
        YearMonth payrollMonth = YearMonth.of(2023, 5);
        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Something went wrong")));

        // Act
        // Assert
        assertThatException().isThrownBy(() -> receiptService.getAllReceiptsInRange(payrollMonth));
        verify(logger).warn(eq("Error retrieving receipts from ZEP"), any(Throwable.class));
    }

    @Test
    void getAllReceiptsInRange_whenTooManyRequestsExceptionIsThrown_thenLogErrorAndReturnEmptyList() {
        // Arrange
        YearMonth payrollMonth = YearMonth.of(2023, 5);
        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Too many requests")));

        // Act
        // Assert
        assertThatException().isThrownBy(() -> receiptService.getAllReceiptsInRange(payrollMonth));
        verify(logger).warn(eq("Error retrieving receipts from ZEP"), any(Throwable.class));
    }

    @Test
    void getAttachmentByReceiptId_whenAttachmentExists_thenReturnAttachment() {
        // Arrange
        int receiptId = 1;
        ZepReceiptAttachment attachment = ZepReceiptAttachment.builder()
                .fileContent("file-content")
                .build();

        when(zepReceiptRestClient.getAttachmentForReceipt(receiptId))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(attachment, null)));

        // Act
        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId, "foo");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().fileContent()).isEqualTo("file-content");
    }

    @Test
    void getAttachmentByReceiptId_whenNoAttachmentExists_thenReturnEmptyOptional() {
        // Arrange
        int receiptId = 1;
        when(zepReceiptRestClient.getAttachmentForReceipt(receiptId))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(null, null)));

        // Act
        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId, "foo");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAttachmentByReceiptId_whenExceptionIsThrown_thenLogErrorAndReturnEmptyOptional() {
        // Arrange
        int receiptId = 1;
        when(zepReceiptRestClient.getAttachmentForReceipt(receiptId))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Something went wrong")));

        // Act
        // Assert
        assertThatException().isThrownBy(() -> receiptService.getAttachmentByReceiptId(receiptId, "foo"));
        verify(logger).warn(eq("Error retrieving attachments for receipt"), any(Throwable.class));
    }
}
