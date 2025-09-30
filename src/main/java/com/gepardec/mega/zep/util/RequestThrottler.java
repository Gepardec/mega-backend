package com.gepardec.mega.zep.util;

import com.gepardec.mega.zep.ZepServiceException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class RequestThrottler {
    int rate;

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void throttle() {
        if (rate >= 10) {
            return;
        }

        if (rate <= 0) {
            throw new ZepServiceException("No requests left!");
        }

        long sleepTime = (long) (10 / (Math.exp(rate / 2.5)) * 1000);
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException ignored) {
            // nop
        }
    }
}
