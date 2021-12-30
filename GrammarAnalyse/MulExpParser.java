package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.MulExpNode;
import WordAnalyse.WordAnalyzer;

public class MulExpParser extends GrammarUnit {
    private final MulExpNode treeNode;

    public MulExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new MulExpNode(null, father);
        treeNode.addUnary(null, (new UnaryExpParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
        while (checkWord("MULT", "DIV", "MOD")) {
            treeNode.addUnary(word.getType(), (new UnaryExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public MulExpParser(WordAnalyzer wordAnalyzer, TreeNode father, TreeNode preReadNode) {
        super(wordAnalyzer);
        treeNode = new MulExpNode(null, father);
        treeNode.addUnary(null, (new UnaryExpParser(wordAnalyzer, treeNode, preReadNode)).getTreeNode());
        super.log();
        while (checkWord("MULT", "DIV", "MOD")) {
            treeNode.addUnary(word.getType(), (new UnaryExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}