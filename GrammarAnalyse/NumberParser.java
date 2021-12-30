package GrammarAnalyse;

import WordAnalyse.WordAnalyzer;

public class NumberParser extends GrammarUnit {
    private final int value;

    public NumberParser(WordAnalyzer wordAnalyzer) {
        super(wordAnalyzer);
        if (!checkWord("INTCON")) {
            handleError(this.getClass(), 0);
        }
        value = Integer.parseInt(word.getSourceWord());
        super.log();
    }

    public int getValue() {
        return value;
    }
}