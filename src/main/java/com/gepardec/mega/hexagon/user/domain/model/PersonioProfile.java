package com.gepardec.mega.hexagon.user.domain.model;

public record PersonioProfile(
        int personioId,
        double vacationDayBalance,
        String guildLead,
        String internalProjectLead,
        boolean hasCreditCard
) {
}
