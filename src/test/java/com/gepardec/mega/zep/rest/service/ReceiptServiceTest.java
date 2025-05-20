package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.ZepServiceTooManyRequestsException;
import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAmount;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
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
import static org.mockito.ArgumentMatchers.any;
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
    ResponseParser responseParser;

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

        when(responseParser.retrieveAll(any(), eq(ZepReceipt.class)))
                .thenReturn(receipts);

        // Act
        List<ZepReceipt> result = receiptService.getAllReceiptsInRange(payrollMonth);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).id()).isEqualTo(1);
        assertThat(result.get(1).id()).isEqualTo(2);
    }

    @Test
    void getAllReceiptsInRange_whenExceptionIsThrown_thenLogErrorAndReturnEmptyList() {
        // Arrange
        YearMonth payrollMonth = YearMonth.of(2023, 5);
        when(responseParser.retrieveAll(any(), eq(ZepReceipt.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        // Act
        List<ZepReceipt> result = receiptService.getAllReceiptsInRange(payrollMonth);

        // Assert
        assertThat(result).isEmpty();
        verify(logger).warn(anyString());
    }

    @Test
    void getAllReceiptsInRange_whenTooManyRequestsExceptionIsThrown_thenLogErrorAndReturnEmptyList() {
        // Arrange
        YearMonth payrollMonth = YearMonth.of(2023, 5);
        when(responseParser.retrieveAll(any(), eq(ZepReceipt.class)))
                .thenThrow(new ZepServiceTooManyRequestsException("Too many requests"));

        // Act
        List<ZepReceipt> result = receiptService.getAllReceiptsInRange(payrollMonth);

        // Assert
        assertThat(result).isEmpty();
        verify(logger).warn(anyString());
    }

    @Test
    void getAttachmentByReceiptId_whenAttachmentExists_thenReturnAttachment() {
        // Arrange
        int receiptId = 1;
        ZepReceiptAttachment attachment = ZepReceiptAttachment.builder()
                .fileContent("file-content")
                .build();

        when(responseParser.retrieveSingle(any(), eq(ZepReceiptAttachment.class)))
                .thenReturn(Optional.of(attachment));

        // Act
        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().fileContent()).isEqualTo("file-content");
    }

    @Test
    void getAttachmentByReceiptId_whenNoAttachmentExists_thenReturnEmptyOptional() {
        // Arrange
        int receiptId = 1;
        when(responseParser.retrieveSingle(any(), eq(ZepReceiptAttachment.class)))
                .thenReturn(Optional.empty());

        // Act
        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAttachmentByReceiptId_whenExceptionIsThrown_thenLogErrorAndReturnEmptyOptional() {
        // Arrange
        int receiptId = 1;
        when(responseParser.retrieveSingle(any(), eq(ZepReceiptAttachment.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        // Act
        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId);

        // Assert
        assertThat(result).isEmpty();
        verify(logger).warn(anyString());
    }

    @Test
    void getAmountByReceiptId_whenAmountExists_thenReturnAmount() {
        // Arrange
        int receiptId = 1;
        ZepReceiptAmount[] amounts = new ZepReceiptAmount[]{
                ZepReceiptAmount.builder()
                        .receiptId(1)
                        .amount(100.0)
                        .quantity(1.0)
                        .build()
        };

        when(responseParser.retrieveSingle(any(), eq(ZepReceiptAmount[].class)))
                .thenReturn(Optional.of(amounts));

        // Act
        Optional<ZepReceiptAmount> result = receiptService.getAmountByReceiptId(receiptId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().receiptId()).isEqualTo(1);
        assertThat(result.get().amount()).isEqualTo(100.0);
        assertThat(result.get().quantity()).isEqualTo(1.0);
    }

    @Test
    void getAmountByReceiptId_whenNoAmountExists_thenReturnEmptyOptional() {
        // Arrange
        int receiptId = 1;
        when(responseParser.retrieveSingle(any(), eq(ZepReceiptAmount[].class)))
                .thenReturn(Optional.empty());

        // Act
        Optional<ZepReceiptAmount> result = receiptService.getAmountByReceiptId(receiptId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAmountByReceiptId_whenExceptionIsThrown_thenLogErrorAndReturnEmptyOptional() {
        // Arrange
        int receiptId = 1;
        when(responseParser.retrieveSingle(any(), eq(ZepReceiptAmount[].class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        // Act
        Optional<ZepReceiptAmount> result = receiptService.getAmountByReceiptId(receiptId);

        // Assert
        assertThat(result).isEmpty();
        verify(logger).warn(anyString());
    }
}
