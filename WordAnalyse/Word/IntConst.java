package WordAnalyse.Word;

public class IntConst extends Word {
    protected static final String PATTERN = "([1-9][0-9]*)|(0)";

    protected IntConst(int line, int startIndex, int endIndex, String word) {
        super(line, startIndex, endIndex, word);
    }

    @Override
    public String getType() {
        return "INTCON";
    }

    @Override
    public String getSourceWord() {
        return sourceWord;
    }
}