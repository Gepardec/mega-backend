package com.gepardec.mega.zep.rest.service;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.ZepServiceTooManyRequestsException;
import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAmount;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.gepardec.mega.domain.utils.DateUtils.*;

@ApplicationScoped
public class ReceiptService {
    @RestClient
    ZepReceiptRestClient zepReceiptRestClient;

    @Inject
    MonthlyReportService monthlyReportService;

    @Inject
    ResponseParser responseParser;

    @Inject
    Logger logger;

    public List<ZepReceipt> getAllReceiptsForYearMonth(Employee employee, String fromDate, String toDate) {
        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);
        LocalDate midOfCurrentMonth = LocalDate.now().withDayOfMonth(14);

        String dateForSearchRequestFrom;
        String dateForSearchRequestTo;

        if(fromDate == null && toDate == null) {
            if (now.isAfter(midOfCurrentMonth) && monthlyReportService.isMonthConfirmedFromEmployee(employee, firstOfPreviousMonth)) {
                dateForSearchRequestFrom = getFirstDayOfCurrentMonth(now);
                dateForSearchRequestTo = getLastDayOfCurrentMonth(now);
            } else {
                dateForSearchRequestTo = "";
                dateForSearchRequestFrom = "";
            }
        } else {
            dateForSearchRequestFrom = fromDate;
            dateForSearchRequestTo = toDate;
        }

        try {
            return responseParser.retrieveAll(
                    page -> zepReceiptRestClient.getAllReceiptsForMonth(dateForSearchRequestFrom, dateForSearchRequestTo, page),
                    ZepReceipt.class
            );
        } catch (ZepServiceTooManyRequestsException | ZepServiceException e) {
            logger.warn(e.getMessage());
        }
        return List.of();
    }

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
