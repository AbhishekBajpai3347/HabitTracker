package org.habitTracker.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportExporter {

    public static void exportToFile(String fileName, String content) {
        try {
            File folder = new File("reports");
            if (!folder.exists()) {
                folder.mkdir();
            }

            BufferedWriter writer = new BufferedWriter(
                    new FileWriter("reports/" + fileName)
            );

            writer.write(content);
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException("Error writing report file.");
        }
    }
}