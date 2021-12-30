package WordAnalyse;

public class UnknownWordFormatException extends Exception {
    private final int line;
    private final int index;

    public UnknownWordFormatException(int line, int index) {
        this.index = index;
        this.line = line + 1;
    }

    @Override
    public String toString() {
        String ret = "unknown word format at line " + line + " index " + index;
        return ret + ", bad char ignored";
    }
}