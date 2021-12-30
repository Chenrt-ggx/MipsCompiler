package Global;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.io.BufferedWriter;

public class LogBuffer {
    public static final LinkedList<String> code = new LinkedList<>();
    public static final LinkedList<String> buffer = new LinkedList<>();

    public static void writeFile(String name, LinkedList<String> log) {
        if (log.isEmpty()) {
            return;
        }
        try {
            File file = new File(name);
            BufferedWriter outputFile = new BufferedWriter(new FileWriter(file), 1 << 16);
            for (String i : log) {
                outputFile.write(i + '\n');
            }
            outputFile.close();
        }
        catch (IOException e) {
            System.err.println("failed to write file " + name);
            e.printStackTrace();
        }
    }
}