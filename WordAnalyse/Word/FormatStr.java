package WordAnalyse.Word;

public class FormatStr extends Word {
    protected static final String PATTERN = "\".*?\"";

    protected FormatStr(int line, int startIndex, int endIndex, String word) {
        super(line, startIndex, endIndex, word);
    }

    @Override
    public String getType() {
        return "STRCON";
    }

    @Override
    public String getSourceWord() {
        return sourceWord;
    }
}