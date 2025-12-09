package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class ReceiptService {

    @RestClient
    ZepReceiptRestClient zepReceiptRestClient;

    @Inject
    Logger logger;

    public List<ZepReceipt> getAllReceiptsInRange(YearMonth payrollMonth) {
        var startDate = payrollMonth.atDay(1).toString();
        var endDate = payrollMonth.atEndOfMonth().toString();

        return Multi.createBy().repeating()
                .uni(AtomicInteger::new, page ->
                        zepReceiptRestClient.getAllReceiptsForMonth(startDate, endDate, page.incrementAndGet())
                                .onFailure().invoke(e1 -> logger.warn("Error retrieving receipts from ZEP", e1))
                )
                .whilst(ZepResponse::hasNext)
                .map(ZepResponse::data)
                .onItem().<ZepReceipt>disjoint()
                .collect().asList()
                .await().indefinitely();
    }

    public Optional<ZepReceiptAttachment> getAttachmentByReceiptId(int receiptId, String filename) {
        if (StringUtils.isEmpty(filename)) {
            return Optional.empty();
        }

        return zepReceiptRestClient.getAttachmentForReceipt(receiptId)
                .onFailure().invoke(e -> logger.warn("Error retrieving attachments for receipt", e))
                .map(response -> Optional.ofNullable(response.data()))
                .onItem().ifNull().continueWith(Optional::empty)
                .await().indefinitely();
    }
}
