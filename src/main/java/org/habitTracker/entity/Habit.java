package org.habitTracker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "habit")
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String type;
    private LocalDate createdAt;
    private int targetFrequency;

    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<HabitLog> logs;

    public Habit(){}

    public Habit(String name, String type, int targetFrequency) {
        this.name = name;
        this.type = type;
        this.createdAt = LocalDate.now();
        this.targetFrequency = targetFrequency;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public int getTargetFrequency() {
        return targetFrequency;
    }

    public void setTargetFrequency(int targetFrequency) {
        this.targetFrequency = targetFrequency;
    }
}
