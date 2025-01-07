package com.example.countdownapp;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Congregation {
    private String name;
    private DayOfWeek[] meetingDays;
    private LocalTime[] meetingTimes;

    public Congregation(String name, DayOfWeek[] meetingDays, LocalTime[] meetingTimes) {
        this.name = name;
        this.meetingDays = meetingDays;
        this.meetingTimes = meetingTimes;
    }

    public String getName() {
        return name;
    }

    public DayOfWeek[] getMeetingDays() {
        return meetingDays;
    }

    public LocalTime[] getMeetingTimes() {
        return meetingTimes;
    }

    public boolean hasMeetingToday(DayOfWeek currentDayOfWeek) {
        if (meetingDays == null || meetingDays.length == 0) {
            return false;
        }

        for (DayOfWeek meetingDay : meetingDays) {
            if (meetingDay == currentDayOfWeek) {
                return true;
            }
        }
        return false;
    }

    public LocalTime getMeetingTime(DayOfWeek currentDayOfWeek) {
        for (int i = 0; i < meetingDays.length; i++) {
            if (meetingDays[i] == currentDayOfWeek) {
                return meetingTimes[i];
            }
        }
        return null; // Se non c'è un incontro per oggi
    }
}
