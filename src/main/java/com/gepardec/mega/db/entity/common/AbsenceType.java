package com.gepardec.mega.db.entity.common;

import java.util.List;

public enum AbsenceType {
    COMPENSATORY_DAYS (0, "FA"),
    HOME_OFFICE_DAYS (1, "HO"),
    VACATION_DAYS(2, "UB"),
    NURSING_DAYS(3, "PU"),
    MATERNITY_LEAVE_DAYS(4,"KA"),
    EXTERNAL_TRAINING_DAYS(5, "EW"),
    CONFERENCE_DAYS(6, "KO"),
    MATERNITY_PROTECTION_DAYS(7, "MU"),
    FATHER_MONTH_DAYS(8, "PA"),
    PAID_SPECIAL_LEAVE_DAYS(9, "SU"),
    NON_PAID_VACATION_DAYS(10, "UU"),
    PAID_SICK_LEAVE(11, "KR");

    final int absenceId;
    final String absenceName;

    AbsenceType(int absenceId, String absenceName){
        this.absenceId = absenceId;
        this.absenceName = absenceName;
    }

    public String getAbsenceName(){return this.absenceName;}
    public int getAbsenceId(){return  this.absenceId;}

    public static List<AbsenceType> getAbsenceTypesWhereWorkingTimeNeeded(){
        return List.of(
                HOME_OFFICE_DAYS,
                EXTERNAL_TRAINING_DAYS
        );
    }
}
