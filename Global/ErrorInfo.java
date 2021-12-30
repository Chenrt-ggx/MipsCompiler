package Global;

import java.util.LinkedList;
import java.util.Comparator;

public class ErrorInfo {
    private static final LinkedList<Pair<Integer, String>> errors = new LinkedList<>();

    public static void push(Integer line, String error) {
        errors.add(new Pair<>(line, error));
    }

    public static boolean logIfHasError() {
        if (errors.isEmpty()) {
            return false;
        }
        errors.sort(Comparator.comparing(Pair::getKey));
        if (Settings.courseSpecificErrorToFile) {
            errors.forEach(i -> LogBuffer.buffer.add(i.getKey() + " " + i.getValue()));
        }
        return true;
    }
}