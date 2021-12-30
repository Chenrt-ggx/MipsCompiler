package GrammarAnalyse;

import SyntaxTree.TreeNode;
import WordAnalyse.WordAnalyzer;

public class FuncFParamsParser extends GrammarUnit {
    public FuncFParamsParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        new FuncFParamParser(wordAnalyzer, father);
        while (checkWord("COMMA")) {
            new FuncFParamParser(wordAnalyzer, father);
        }
        super.log();
    }
}