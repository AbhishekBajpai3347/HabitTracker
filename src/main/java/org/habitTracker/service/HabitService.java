package org.habitTracker.service;

import org.habitTracker.dao.HabitDAO;
import org.habitTracker.dao.HabitLogDAO;
import org.habitTracker.entity.Habit;
import org.habitTracker.entity.HabitLog;
import org.habitTracker.util.ReportExporter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;

public class HabitService {


    private final HabitDAO habitDAO = new HabitDAO();
    private final HabitLogDAO logDAO = new HabitLogDAO();

    public void addHabit(String name, String type, int targetFrequency){
        if (targetFrequency <= 0 ||
                (!"DAILY".equals(type) && !"WEEKLY".equals(type))) {
            throw new IllegalArgumentException("Invalid habit type.");
        }
        Habit habit = new Habit(name, type, targetFrequency);
        habitDAO.saveHabit(habit);
    }

    public void removeHabit(long habitId){
            habitDAO.deleteHabit(habitId);
    }

    public void markHabitDone(long habitId, LocalDate date){
        Habit habit = habitDAO.getHabitById(habitId);
        if (habit == null) {
            throw new IllegalArgumentException("Habit with ID " + habitId + " not found.");
        }
        int prevCount = logDAO.getLogsCompletionCount(habitId, date);
        HabitLog log = new HabitLog(date, prevCount+1, habit);

        if(prevCount == 0) logDAO.saveLog(log);
        else{
            logDAO.updateCompletionCount(habitId, date, prevCount+1);
        }
        System.out.println("Progress today : " + (prevCount+1) + "/" + habit.getTargetFrequency());
    }

    public List<Habit> getAllHabits() {
        return habitDAO.getAllHabits();
    }

    public boolean hasCompletedTargetForToday(long habitId) {
        Habit habit = habitDAO.getHabitById(habitId);

        int logCompletionCount = logDAO.getLogsCompletionCount(habitId, LocalDate.now());
        if (habit == null) {
            throw new IllegalArgumentException("Habit with ID " + habitId + " not found.");
        }
        return logCompletionCount >= habit.getTargetFrequency();
    }

    public int calculateCurrentStreak(long habitId) {
        List<HabitLog> logs = logDAO.getLogsByHabit(habitId);
        Habit habit = habitDAO.getHabitById(habitId);
        int targetFrequency = habit.getTargetFrequency();

        if (habit.getType().equals("DAILY")){
            int streak_counter = 0;
            LocalDate prevDate = LocalDate.now();

            for (HabitLog log : logs) {
                LocalDate logDate = log.getDate();
                int completionCount = log.getCompletionCount();
                long diff = ChronoUnit.DAYS.between(logDate, prevDate);
                if (diff == 0 || diff == 1 && completionCount >= targetFrequency) {
                    streak_counter++;
                    prevDate = logDate;
                }else break;
            }
            return streak_counter;
        } else if(habit.getType().equals("WEEKLY")) {
            return calculateWeeklyCurrentStreak(habitId);
        } else {
            return -1;
        }
    }

    private Map<LocalDate, Integer> getWeeklyTotals(long habitId) {
        List<HabitLog> logs = logDAO.getLogsByHabit(habitId);
        Map<LocalDate, Integer> weeklyTotals = new HashMap<>();
        for (HabitLog log : logs) {
            LocalDate weekStart = log.getDate()
                    .with(WeekFields.ISO.dayOfWeek(), 1);
            weeklyTotals.merge(
                    weekStart,
                    log.getCompletionCount(),
                    Integer::sum
            );
        }

        return weeklyTotals;
    }


    private int calculateWeeklyCurrentStreak(long habitId) {
        Habit habit = habitDAO.getHabitById(habitId);
        Map<LocalDate, Integer> weeklyTotals = getWeeklyTotals(habitId);

        if (weeklyTotals.isEmpty()) return 0;

        List<LocalDate> weeks = new ArrayList<>(weeklyTotals.keySet());
        weeks.sort(Comparator.reverseOrder());

        int streak = 0;

        LocalDate currentWeek = LocalDate.now()
                .with(WeekFields.ISO.dayOfWeek(), 1);

        for (LocalDate week : weeks) {
            if (ChronoUnit.WEEKS.between(week, currentWeek) == 0) {
                if (weeklyTotals.get(week) >= habit.getTargetFrequency()) {
                    streak = 1;
                    currentWeek = week;
                } else {
                    return 0;
                }
            } else {
                break;
            }
        }

        for (int i = 1; i < weeks.size(); i++) {

            LocalDate prev = weeks.get(i - 1);
            LocalDate curr = weeks.get(i);

            if (ChronoUnit.WEEKS.between(curr, prev) == 1 &&
                    weeklyTotals.get(curr) >= habit.getTargetFrequency()) {

                streak++;
            } else {
                break;
            }
        }

        return streak;
    }


    private int calculateWeeklyLongestStreak(long habitId) {

        Habit habit = habitDAO.getHabitById(habitId);
        Map<LocalDate, Integer> weeklyTotals = getWeeklyTotals(habitId);

        if (weeklyTotals.isEmpty()) return 0;

        List<LocalDate> weeks = new ArrayList<>(weeklyTotals.keySet());
        weeks.sort(Comparator.reverseOrder());

        int maxStreak = 0;
        int currentStreak = 0;

        for (int i = 0; i < weeks.size(); i++) {

            LocalDate week = weeks.get(i);
            boolean success = weeklyTotals.get(week) >= habit.getTargetFrequency();

            if (!success) {
                currentStreak = 0;
                continue;
            }

            if (i == 0) {
                currentStreak = 1;
            } else {
                LocalDate prev = weeks.get(i - 1);

                if (ChronoUnit.WEEKS.between(week, prev) == 1) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
            }

            maxStreak = Math.max(maxStreak, currentStreak);
        }

        return maxStreak;
    }

    public int longestStreak(long habitId) {
        List<HabitLog> logs = logDAO.getLogsByHabit(habitId);
        if (logs == null || logs.isEmpty()) {
            return 0;
        }
        if (logs.size() == 1) {
            return 1;
        }
        int longest_streak = 1;
        int currentStreak = 1;
        int n = logs.size();
        LocalDate currentDate = logs.get(0).getDate();
        LocalDate prevDate = logs.get(1).getDate();
        Habit habit = habitDAO.getHabitById(habitId);

        if (habit.getType().equals("DAILY")){
            int targetFrequency = habit.getTargetFrequency();

            int i = 1;
            while (i < n) {
                long diff = ChronoUnit.DAYS.between(prevDate, currentDate);
                if (diff == 1 && logs.get(i).getCompletionCount() >= targetFrequency) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
                longest_streak = Math.max(longest_streak, currentStreak);
                currentDate = prevDate;
                i++;
                if (i < n) {
                    prevDate = logs.get(i).getDate();
                }
            }
            return longest_streak;
        } else if(habit.getType().equals("WEEKLY")){
            return calculateWeeklyLongestStreak(habitId);
        } else {
            return -1;
        }
    }

    public void exportHabitReport(long habitId) {

        Habit habit = habitDAO.getHabitById(habitId);

        if (habit == null) {
            throw new IllegalArgumentException("Habit not found.");
        }

        List<HabitLog> logs = logDAO.getLogsByHabit(habitId);

        int currentStreak = calculateCurrentStreak(habitId);
        int longestStreak = longestStreak(habitId);
        double completionPercentage = calculateCompletionPercentage(habitId);

        StringBuilder report = new StringBuilder();

        report.append("===== Habit Report =====\n\n");
        report.append("Habit Name: ").append(habit.getName()).append("\n");
        report.append("Type: ").append(habit.getType()).append("\n");
        report.append("Target Frequency: ").append(habit.getTargetFrequency()).append("\n");
        report.append("Created At: ").append(habit.getCreatedAt()).append("\n\n");

        report.append("Current Streak: ").append(currentStreak).append("\n");
        report.append("Longest Streak: ").append(longestStreak).append("\n");
        report.append(String.format("Completion Percentage: %.2f%%\n\n", completionPercentage));

        report.append("----- Logs -----\n");

        for (HabitLog log : logs) {
            boolean success = log.getCompletionCount() >= habit.getTargetFrequency();
            report.append(log.getDate())
                    .append(" -> ")
                    .append(log.getCompletionCount())
                    .append(success ? " (SUCCESS)" : " (INCOMPLETE)")
                    .append("\n");
        }

        String fileName = "habit_" + habitId + "_report.txt";

        ReportExporter.exportToFile(fileName, report.toString());
    }

    private double calculateCompletionPercentage(long habitId) {

        Habit habit = habitDAO.getHabitById(habitId);

        if (habit == null) {
            throw new IllegalArgumentException("Habit not found.");
        }

        LocalDate today = LocalDate.now();
        LocalDate createdAt = habit.getCreatedAt();

        if (habit.getType().equals("DAILY")) {

            List<HabitLog> logs = logDAO.getLogsByHabit(habitId);

            long successfulDays = logs.stream()
                    .filter(log -> log.getCompletionCount() >= habit.getTargetFrequency())
                    .count();

            long totalDays = ChronoUnit.DAYS.between(createdAt, today) + 1;

            return totalDays == 0 ? 0 :
                    ((double) successfulDays / totalDays) * 100.0;

        } else {

            Map<LocalDate, Integer> weeklyTotals = getWeeklyTotals(habitId);

            int successfulWeeks = 0;

            for (Integer total : weeklyTotals.values()) {
                if (total >= habit.getTargetFrequency()) {
                    successfulWeeks++;
                }
            }

            LocalDate creationWeek = createdAt
                    .with(WeekFields.ISO.dayOfWeek(), 1);

            LocalDate currentWeek = today
                    .with(WeekFields.ISO.dayOfWeek(), 1);

            long totalWeeks = ChronoUnit.WEEKS
                    .between(creationWeek, currentWeek) + 1;

            return totalWeeks == 0 ? 0 :
                    ((double) successfulWeeks / totalWeeks) * 100.0;
        }
    }


    public void printHabitSummary(long habitId) {

        List<HabitLog> logs = logDAO.getLogsByHabit(habitId);

        if (logs == null || logs.isEmpty()) {
            System.out.println("No logs found.");
            System.out.println("Current Streak : 0");
            System.out.println("Longest Streak : 0");
            System.out.println("Completion Percentage : 0%");
            System.out.println("Total logs : 0");
            return;
        }

        long n = logs.stream()
                .filter(log -> log.getCompletionCount() >= habitDAO.getHabitById(habitId).getTargetFrequency())
                .count();

        System.out.println("Current Streak : " + calculateCurrentStreak(habitId));
        System.out.println("Longest Streak : " + longestStreak(habitId));

        System.out.println("Completion Percentage: %.2f%%\n" + calculateCompletionPercentage(habitId));
        System.out.println("Total logs : " + n);
    }

}
