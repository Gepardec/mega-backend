package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.gepardec.mega.domain.utils.DateUtils.*;

@ApplicationScoped
public class ReceiptService {
    @RestClient
    ZepReceiptRestClient zepReceiptRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public List<ZepReceipt> getAllReceiptsForYearMonth(YearMonth yearMonth) {
        String startDate = getFirstDayOfCurrentMonth(LocalDate.now());
        String endDate = formatDate(getLastDayOfCurrentMonth(startDate));

        if(yearMonth != null){
            startDate = formatDate(yearMonth.atDay(1));
            endDate = formatDate(getLastDayOfCurrentMonth(startDate));
        }

        try {
            String finalStartDate = startDate;
            String finalEndDate = endDate;
            return responseParser.retrieveAll(
                    page -> zepReceiptRestClient.getAllReceiptsForMonth(finalStartDate, finalEndDate, page),
                    ZepReceipt.class
            );
        } catch (ZepServiceException e) {
            assert yearMonth != null;
            logger.warn("Error retrieving receipts for month + \"%d\" from ZEP: No /data field in response"
                    .formatted(yearMonth.getMonth().getValue()));
        }
        return List.of();
    }
}
