package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.AddExpNode;
import WordAnalyse.WordAnalyzer;

public class ExpParser extends GrammarUnit {
    private final AddExpNode treeNode;

    public ExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = (AddExpNode)(new AddExpParser(wordAnalyzer, father)).getTreeNode();
        super.log();
    }

    public ExpParser(WordAnalyzer wordAnalyzer, TreeNode father, TreeNode preReadNode) {
        super(wordAnalyzer);
        treeNode = (AddExpNode)(new AddExpParser(wordAnalyzer, father, preReadNode)).getTreeNode();
        super.log();
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}