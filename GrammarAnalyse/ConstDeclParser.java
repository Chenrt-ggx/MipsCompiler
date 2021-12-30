package GrammarAnalyse;

import SyntaxTree.TreeNode;
import WordAnalyse.WordAnalyzer;

public class ConstDeclParser extends GrammarUnit {
    public ConstDeclParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        if (!checkWord("CONSTTK")) {
            handleError(this.getClass(), 0);
        }
        if (!checkWord("INTTK")) {
            handleError(this.getClass(), 1);
        }
        new ConstDefParser(wordAnalyzer, father);
        while (checkWord("COMMA")) {
            new ConstDefParser(wordAnalyzer, father);
        }
        if (!checkWord("SEMICN")) {
            handleError(this.getClass(), 2);
        }
        super.log();
    }
}