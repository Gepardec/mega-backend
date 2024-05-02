package com.gepardec.mega.db.entity.common;

import java.util.Optional;
import java.util.stream.Stream;

public enum PaymentMethodType {
    COMPANY (0, "Firma"),
    PRIVATE (1, "privat");

    final int paymentMethodId;
    final String paymentMethodName;

    PaymentMethodType(int paymentMethodId, String paymentMethodName){
        this.paymentMethodId = paymentMethodId;
        this.paymentMethodName = paymentMethodName;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public static Optional<PaymentMethodType> getByName(String paymentMethodName) {
        return Stream.of(values()).filter(v -> v.paymentMethodName.equals(paymentMethodName)).findFirst();
    }
}
