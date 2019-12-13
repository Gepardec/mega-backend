package com.gepardec.mega.monthlyreport;

import com.gepardec.mega.utils.DateUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.gepardec.mega.monthlyreport.TimeWarning.*;

class WarningCalculator {

    private List<TimeWarning> timeWarnings;
    private List<JourneyWarning> journeyWarnings;

    private WarningConfig warningConfig;

    @PostConstruct
    private void initWarningConfig() {
        BeanManager bm = CDI.current().getBeanManager();
        Bean<WarningConfig> bean = (Bean<WarningConfig>) bm.getBeans(WarningConfig.class).iterator().next();
        CreationalContext<WarningConfig> ctx = bm.createCreationalContext(bean);
        warningConfig = (WarningConfig) bm.getReference(bean, WarningConfig.class, ctx);
    }

    public List<TimeWarning> determineTimeWarnings(ProjectTimeManager projectTimeManager) {
        timeWarnings = new ArrayList<>(0);
        Set<Map.Entry<LocalDate, List<ProjectTimeEntry>>> entries = projectTimeManager.getProjectTimes().entrySet();

        //1. more than 10 hours a day
        entries.forEach(e -> checkFor10Hours(e.getValue(), e.getKey()));

        //2. no break when more than 6 hours a day
        entries.forEach(e -> checkForBreaksForWorkingDaysWithMoreThan6Hours(e.getValue(), e.getKey()));

        //3. check earliest start and latest ending
        projectTimeManager.getEntriesAsFlatList().forEach(e -> checkForFlexibleWorkFrame(e));

        //4. check fore enough rest
        checkForRestTime(projectTimeManager);

        timeWarnings.sort(Comparator.comparing(TimeWarning::getDate));
        return timeWarnings;
    }


    private void checkFor10Hours(List<ProjectTimeEntry> entriesPerDay, LocalDate date) {
        double workDurationOfDay = calcWorkingDurationOfDay(entriesPerDay);
        if (workDurationOfDay > MAX_HOURS_A_DAY) {
            TimeWarning timeWarning = new TimeWarning();
            timeWarning.setDate(date);
            timeWarning.setExcessWorkTime(workDurationOfDay - MAX_HOURS_A_DAY);
            addToTimeWarnings(timeWarning);
        }
    }


    private void checkForBreaksForWorkingDaysWithMoreThan6Hours(List<ProjectTimeEntry> entriesPerDay, LocalDate date) {
        double workHoursOfDay = calcWorkingDurationOfDay(entriesPerDay);
        if (workHoursOfDay > MAX_HOURS_OF_DAY_WITHOUT_BREAK) {
            double breakTime = 0d;
            for (int i = 0; i < entriesPerDay.size(); i++) {
                ProjectTimeEntry actualEntry = entriesPerDay.get(i);
                if (i + 1 < entriesPerDay.size()) {
                    ProjectTimeEntry nextEntry = entriesPerDay.get(i + 1);
                    breakTime += DateUtils.calcDiffInHours(actualEntry.getToTime(), nextEntry.getFromTime());

                }
            }
            if (breakTime < MIN_REQUIRED_BREAK_TIME) {
                TimeWarning timeWarning = new TimeWarning();
                timeWarning.setDate(date);
                timeWarning.setMissingBreakTime(MIN_REQUIRED_BREAK_TIME - breakTime);
                addToTimeWarnings(timeWarning);
            }
        }
    }

    private void checkForFlexibleWorkFrame(ProjectTimeEntry projectTimeEntry) {
        if (projectTimeEntry.getFromTime().toLocalTime().isBefore(EARLIEST_START_TIME)) {
            TimeWarning timeWarning = new TimeWarning();
            timeWarning.setDate(projectTimeEntry.getDate());
            addToTimeWarnings(timeWarning);
        }
        if (projectTimeEntry.getToTime().toLocalTime().isAfter(LATEST_END_TIME)) {
            TimeWarning timeWarning = new TimeWarning();
            timeWarning.setDate(projectTimeEntry.getDate());
            addToTimeWarnings(timeWarning);
        }
    }

    private void checkForRestTime(ProjectTimeManager projectTimeManager) {

        List<ProjectTimeEntry> entries = projectTimeManager.getProjectTimes().values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ProjectTimeEntry::getFromTime))
                .collect(Collectors.toList());

        for (int i = 0; i < entries.size(); i++) {
            ProjectTimeEntry actualEntry = entries.get(i);
            if (i + 1 < entries.size()) {
                ProjectTimeEntry nextEntry = entries.get(i + 1);
                if (nextEntry.getDate().isEqual(actualEntry.getDate().plusDays(1L))) {
                    double restHours = DateUtils.calcDiffInHours(actualEntry.getToTime(), nextEntry.getFromTime());
                    if (restHours < MIN_REQUIRED_REST_TIME) {
                        TimeWarning timeWarning = new TimeWarning();
                        timeWarning.setDate(nextEntry.getDate());
                        timeWarning.setMissingRestTime(MIN_REQUIRED_REST_TIME - restHours);
                        addToTimeWarnings(timeWarning);
                    }
                }
            }
        }
    }


    private void addToTimeWarnings(TimeWarning newTimeWarning) {
        Optional<TimeWarning> breakWarning = timeWarnings.stream()
                .filter(bw -> bw.getDate().isEqual(newTimeWarning.getDate()))
                .findAny();

        if (breakWarning.isPresent()) {
            breakWarning.get().mergeBreakWarnings(newTimeWarning);
        } else {
            timeWarnings.add(newTimeWarning);
        }
    }

    private static double calcWorkingDurationOfDay(List<ProjectTimeEntry> entriesPerDay) {
        double dayWorkingDuration = 0.0d;
        for (ProjectTimeEntry entry : entriesPerDay) {
            if (!Task.isJourney(entry.getTask())) {
                dayWorkingDuration += entry.getDurationInHours();
            }
        }
        return dayWorkingDuration;
    }


    public List<JourneyWarning> determineJourneyWarnings(ProjectTimeManager projectTimeManager) {
        journeyWarnings = new ArrayList<>(0);
        JourneyDirectionHandler journeyDirectionHandler = new JourneyDirectionHandler();
        for (ProjectTimeEntry projectTimeEntry : projectTimeManager.getEntriesAsFlatList()) {

            if (projectTimeEntry instanceof JourneyEntry) {
                JourneyEntry journeyEntry = (JourneyEntry) projectTimeEntry;
                journeyDirectionHandler.moveTo(journeyEntry.getJourneyDirection())
                        .ifPresent(warning -> addToJourneyWarnings(journeyEntry, warning));
            }
        }
        return journeyWarnings;
    }


    private void addToJourneyWarnings(JourneyEntry journeyEntry, Warning warning) {
        JourneyWarning newJourneyWarning = new JourneyWarning();
        newJourneyWarning.setDate(journeyEntry.getDate());
        newJourneyWarning.getWarnings().add(warningConfig.getTextByWarning(warning));


        Optional<JourneyWarning> journeyWarning = journeyWarnings.stream()
                .filter(warn -> warn.getDate().isEqual(journeyEntry.getDate()))
                .findAny();

        if (journeyWarning.isPresent()) {
            journeyWarning.get().getWarnings().addAll(newJourneyWarning.getWarnings());
        } else {
            journeyWarnings.add(newJourneyWarning);
        }
    }
}
