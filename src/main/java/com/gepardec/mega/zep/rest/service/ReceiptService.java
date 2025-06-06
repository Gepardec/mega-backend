package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.ZepServiceTooManyRequestsException;
import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAmount;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.util.ResponseParser;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReceiptService {
    @RestClient
    ZepReceiptRestClient zepReceiptRestClient;

    @Inject
    ResponseParser responseParser;

    @Inject
    Logger logger;

    @RateLimit(value = 6, window = 1, windowUnit = ChronoUnit.SECONDS)
    public List<ZepReceipt> getAllReceiptsInRange(YearMonth payrollMonth) {
        var startDate = payrollMonth.atDay(1).toString();
        var endDate = payrollMonth.atEndOfMonth().toString();

        try {
            return responseParser.retrieveAll(
                    page -> zepReceiptRestClient.getAllReceiptsForMonth(startDate, endDate, page),
                    ZepReceipt.class
            );
        } catch (ZepServiceTooManyRequestsException | ZepServiceException e) {
            logger.warn(e.getMessage());
        }
        return List.of();
    }

    @RateLimit(value = 6, window = 1, windowUnit = ChronoUnit.SECONDS)
    public Optional<ZepReceiptAttachment> getAttachmentByReceiptId(int receiptId) {
        try {
            return responseParser.retrieveSingle(
                    zepReceiptRestClient.getAttachmentForReceipt(receiptId),
                    ZepReceiptAttachment.class
            );
        } catch (ZepServiceTooManyRequestsException | ZepServiceException e) {
            logger.warn(e.getMessage());
        }
        return Optional.empty();
    }

    @RateLimit(value = 6, window = 1, windowUnit = ChronoUnit.SECONDS)
    public Optional<ZepReceiptAmount> getAmountByReceiptId(int receiptId) {
        try {

            return responseParser.retrieveSingle(
                    zepReceiptRestClient.getAmountForReceipt(receiptId),
                    ZepReceiptAmount[].class
            ).map(x -> x[0]);
        } catch (ZepServiceTooManyRequestsException | ZepServiceException e) {
            logger.warn(e.getMessage());
        }
        return Optional.empty();
    }
}
