package GrammarAnalyse;

import WordAnalyse.WordAnalyzer;

public class UnaryOpParser extends GrammarUnit {
    private final String type;

    public UnaryOpParser(WordAnalyzer wordAnalyzer) {
        super(wordAnalyzer);
        if (!checkWord("NOT", "PLUS", "MINU")) {
            handleError(this.getClass(), 0);
        }
        type = word.getType();
        super.log();
    }

    public String getType() {
        return type;
    }
}