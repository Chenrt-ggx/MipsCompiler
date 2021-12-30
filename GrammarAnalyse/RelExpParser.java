package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.RelExpNode;
import WordAnalyse.WordAnalyzer;

public class RelExpParser extends GrammarUnit {
    private final RelExpNode treeNode;

    public RelExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new RelExpNode(null, father);
        treeNode.addAdd(null, (new AddExpParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
        while (checkWord("LSS", "LEQ", "GRE", "GEQ")) {
            treeNode.addAdd(word.getType(), (new AddExpParser(wordAnalyzer, treeNode)).getTreeNode());
            super.log();
        }
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}