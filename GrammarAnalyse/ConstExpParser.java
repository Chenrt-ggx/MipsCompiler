package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.AddExpNode;
import WordAnalyse.WordAnalyzer;

public class ConstExpParser extends GrammarUnit {
    private final AddExpNode treeNode;

    public ConstExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = (AddExpNode)(new AddExpParser(wordAnalyzer, father)).getTreeNode();
        super.log();
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}