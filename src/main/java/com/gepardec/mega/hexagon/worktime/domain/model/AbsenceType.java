package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum AbsenceType {
    VACATION("UB"),
    PAID_SICK_LEAVE("KR"),
    HOME_OFFICE("HO"),
    EXTERNAL_TRAINING("EW"),
    MATERNITY_LEAVE("KA"),
    NURSING("PU"),
    COMPENSATORY_TIME_OFF("FA"),
    CONFERENCE("KO"),
    MATERNITY_PROTECTION("MU"),
    FATHER_MONTH("PA"),
    PAID_SPECIAL_LEAVE("SU"),
    NON_PAID_VACATION("UU");

    private final String zepCode;

    AbsenceType(String zepCode) {
        this.zepCode = zepCode;
    }

    public static Optional<AbsenceType> fromZepCode(String zepCode) {
        if (zepCode == null || zepCode.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(type -> type.zepCode.equals(zepCode))
                .findFirst();
    }
}
