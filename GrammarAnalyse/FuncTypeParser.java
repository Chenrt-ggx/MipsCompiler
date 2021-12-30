package GrammarAnalyse;

import WordAnalyse.WordAnalyzer;

public class FuncTypeParser extends GrammarUnit {
    private final String type;

    public FuncTypeParser(WordAnalyzer wordAnalyzer) {
        super(wordAnalyzer);
        if (!checkWord("INTTK", "VOIDTK")) {
            handleError(this.getClass(), 0);
        }
        type = word.getType();
        super.log();
    }

    public String getType() {
        return type;
    }
}