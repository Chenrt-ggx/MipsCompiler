package GrammarAnalyse;

import SyntaxTree.TreeNode;
import WordAnalyse.WordAnalyzer;

public class VarDeclParser extends GrammarUnit {
    public VarDeclParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        if (!checkWord("INTTK")) {
            handleError(this.getClass(), 0);
        }
        new VarDefParser(wordAnalyzer, father);
        while (checkWord("COMMA")) {
            new VarDefParser(wordAnalyzer, father);
        }
        if (!checkWord("SEMICN")) {
            handleError(this.getClass(), 1);
        }
        super.log();
    }
}