package org.habitTracker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "habit_log")
public class HabitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDate date;
    private int completionCount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "habit_id")
    private Habit habit;

    public HabitLog(){}

    public HabitLog(LocalDate date, int completionCount, Habit habit){
        this.date = date;
        this.completionCount = completionCount;
        this.habit = habit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public void setCompletionCount(int completionCount) {
        this.completionCount = completionCount;
    }

    @Override
    public String toString() {
        return "HabitLog{" +
                "id=" + id +
                ", date=" + date +
                ", completionCount=" + completionCount +
                ", habit=" + habit +
                '}';
    }
}
