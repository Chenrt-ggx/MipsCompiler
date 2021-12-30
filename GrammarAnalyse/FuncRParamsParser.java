package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.FuncCallNode;
import WordAnalyse.WordAnalyzer;

public class FuncRParamsParser extends GrammarUnit {
    public FuncRParamsParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        ((FuncCallNode)father).addParams((new ExpParser(wordAnalyzer, father)).getTreeNode());
        while (checkWord("COMMA")) {
            ((FuncCallNode)father).addParams((new ExpParser(wordAnalyzer, father)).getTreeNode());
        }
        super.log();
    }
}