package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.LOrExpNode;
import WordAnalyse.WordAnalyzer;

public class CondParser extends GrammarUnit {
    private final LOrExpNode treeNode;

    public CondParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = (LOrExpNode)(new LOrExpParser(wordAnalyzer, father)).getTreeNode();
        super.log();
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}