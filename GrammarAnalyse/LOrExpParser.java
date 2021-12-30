package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.LOrExpNode;
import WordAnalyse.WordAnalyzer;

public class LOrExpParser extends GrammarUnit {
    private final LOrExpNode treeNode;

    public LOrExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new LOrExpNode(null, father);
        treeNode.addLAnd((new LAndExpParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
        while (checkWord("OR")) {
            treeNode.addLAnd((new LAndExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}