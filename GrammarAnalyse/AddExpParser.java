package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.AddExpNode;
import WordAnalyse.WordAnalyzer;

public class AddExpParser extends GrammarUnit {
    private final AddExpNode treeNode;

    public AddExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new AddExpNode(null, father);
        treeNode.addMul(null, (new MulExpParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
        while (checkWord("PLUS", "MINU")) {
            treeNode.addMul(word.getType(), (new MulExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public AddExpParser(WordAnalyzer wordAnalyzer, TreeNode father, TreeNode preReadNode) {
        super(wordAnalyzer);
        treeNode = new AddExpNode(null, father);
        treeNode.addMul(null, (new MulExpParser(wordAnalyzer, treeNode, preReadNode)).getTreeNode());
        super.log();
        while (checkWord("PLUS", "MINU")) {
            treeNode.addMul(word.getType(), (new MulExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}