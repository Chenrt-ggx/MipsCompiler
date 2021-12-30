package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.LAndExpNode;
import WordAnalyse.WordAnalyzer;

public class LAndExpParser extends GrammarUnit {
    private final LAndExpNode treeNode;

    public LAndExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new LAndExpNode(null, father);
        treeNode.addEq((new EqExpParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
        while (checkWord("AND")) {
            treeNode.addEq((new EqExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}