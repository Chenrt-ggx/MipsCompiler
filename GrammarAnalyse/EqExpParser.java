package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.EqExpNode;
import WordAnalyse.WordAnalyzer;

public class EqExpParser extends GrammarUnit {
    private final EqExpNode treeNode;

    public EqExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new EqExpNode(null, father);
        treeNode.addRel(null, (new RelExpParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
        while (checkWord("EQL", "NEQ")) {
            treeNode.addRel(word.getType(), (new RelExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}