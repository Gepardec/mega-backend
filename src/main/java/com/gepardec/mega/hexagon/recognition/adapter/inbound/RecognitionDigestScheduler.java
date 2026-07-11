package com.gepardec.mega.hexagon.recognition.adapter.inbound;

import com.gepardec.mega.hexagon.recognition.application.port.inbound.SendRecognitionDigestUseCase;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RecognitionDigestScheduler {

    private final SendRecognitionDigestUseCase sendRecognitionDigestUseCase;

    @Inject
    public RecognitionDigestScheduler(SendRecognitionDigestUseCase sendRecognitionDigestUseCase) {
        this.sendRecognitionDigestUseCase = sendRecognitionDigestUseCase;
    }

    @Scheduled(identity = "Send weekly recognition digest", cron = "0 0 17 ? * MON")
    void sendWeeklyDigest() {
        Log.info("Starting scheduled recognition digest dispatch");
        sendRecognitionDigestUseCase.sendDigest();
        Log.info("Finished scheduled recognition digest dispatch");
    }
}
