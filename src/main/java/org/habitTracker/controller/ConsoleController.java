package org.habitTracker.controller;

import org.habitTracker.dao.HabitDAO;
import org.habitTracker.dao.HabitLogDAO;
import org.habitTracker.entity.Habit;
import org.habitTracker.service.HabitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static java.awt.Color.RED;

public class ConsoleController {
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final Logger log = LoggerFactory.getLogger(ConsoleController.class);

    HabitService service = new HabitService();
    Scanner sc = new Scanner(System.in);
    int chosenHabit;

    private void showMenu(){
            System.out.println("======= Menu ======");
            System.out.println(CYAN + "1. Add Habit");
            System.out.println("2. Mark Habit Done");
            System.out.println("3. View Habit Summary");
            System.out.println("4. View All Habits");
            System.out.println("5. Export Habit Summary");
            System.out.println("6. Delete Habit");
            System.out.println("7. Exit" + RESET);
    }

    private void displayList(){
        List<Habit> habits = service.getAllHabits();

        String formatString = "| %-5s | %-20s | %-30s | %-16s | %-10s |%n";
        String separator = "+-------+----------------------+--------------------------------+------------------+------------+%n";

        System.out.printf(GREEN + separator);
        System.out.printf(formatString, "ID", "Created At", "Name", "Target Frequency", "Type");
        System.out.printf(separator + RESET);

        for (Habit habit : habits) {
            System.out.printf(formatString,
                    habit.getId(),
                    habit.getCreatedAt(),
                    habit.getName(),
                    habit.getTargetFrequency(),
                    habit.getType());
        }

        System.out.printf(separator);
    }

    private int readInt(String message) {
        while (true) {
            System.out.print(YELLOW + message + RESET);
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private String readString(String message) {
        System.out.println(message);
        return sc.nextLine();
    }

    private void handleAddHabit(){
        System.out.println("\nAdd Habit :-");
        String habitName = readString("Enter the name of that habit : \"");
        String habitType = readString("Enter Habit type [DAILY, WEEKLY]: ");
        int habitFrequency = readInt("Enter habit frequency : ");

        service.addHabit(habitName, habitType, habitFrequency);
        System.out.println("Habit Added Successfully.");
    }

    private void handleDeleteHabit(){
        System.out.println("\nDelete habit :- ");
        displayList();
        int habitId = readInt("Enter the id of habit that you want to delete :- ");
        try{
            service.removeHabit(habitId);
        }catch (Exception e){
            log.error("e: ", e);
        }
    }

    private void handleMarkHabitDone(){
        chosenHabit = readInt("Enter habit ID :- ");
        try {
            service.markHabitDone(chosenHabit, LocalDate.now());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Habit marked done.");

        if (service.hasCompletedTargetForToday(chosenHabit)) {
            System.out.println("Congratulations! You have completed your target for this habit.");
        }
    }

    private void handleShowSummary(){
        System.out.println("Print Summary:- ");
        chosenHabit = readInt("Please chose the habit to build Summary for:- ");
        service.printHabitSummary(chosenHabit);
    }

    private void handleExportHabit() {
        int id = readInt("Enter Habit ID to export: ");
        try {
            service.exportHabitReport(id);
            System.out.println(GREEN + "Report exported successfully." + RESET);
        } catch (IllegalArgumentException e) {
            System.out.println(RED + e.getMessage() + RESET);
        }
    }

    public void AppRunner(){
        boolean exitLoop = false;
        do {
            showMenu();
            int choice = readInt("Enter your choice : ");
            switch (choice) {
                case 1:
                    handleAddHabit();
                    break;
                case 2:
                    displayList();
                    System.out.println("\nMark habit done :-");
                    handleMarkHabitDone();
                    break;
                case 3:
                    displayList();
                    handleShowSummary();
                    break;
                case 4:
                    System.out.println("Display all habits:- ");
                    displayList();
                    break;
                case 5:
                    handleExportHabit();
                    break;
                case 6:
                    handleDeleteHabit();
                    break;
                case 7:
                    exitLoop = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }

        } while (!exitLoop);
        sc.close();
    }
}
