package com.gepardec.mega.domain.model;

import java.util.Optional;
import java.util.stream.Stream;

public enum BillabilityPreset {

    BILLABLE(1),
    BILLABLE_FIXED(2),
    NOT_BILLABLE(3),
    NOT_BILLABLE_FIXED(4);

    private final int zepId;

    BillabilityPreset(int zepId) {
        this.zepId = zepId;
    }

    public static Optional<BillabilityPreset> byZepId(int zepId) {
        return Stream.of(values())
                .filter(billabilityPreset -> billabilityPreset.zepId == zepId)
                .findFirst();
    }

    public static boolean isBillable(BillabilityPreset billabilityPreset) {
        return BILLABLE.equals(billabilityPreset) || BILLABLE_FIXED.equals(billabilityPreset);
    }

    public int getZepId() {
        return zepId;
    }
}
