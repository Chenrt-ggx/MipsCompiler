package WordAnalyse.Word;

public class Ident extends Word {
    protected static final String PATTERN = "[_a-zA-Z][_a-zA-Z0-9]*";

    protected Ident(int line, int startIndex, int endIndex, String word) {
        super(line, startIndex, endIndex, word);
    }

    @Override
    public String getType() {
        return "IDENFR";
    }

    @Override
    public String getSourceWord() {
        return sourceWord;
    }
}