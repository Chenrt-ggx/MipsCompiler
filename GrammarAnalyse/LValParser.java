package GrammarAnalyse;

import SyntaxTree.LValNode;
import SyntaxTree.TreeNode;
import WordAnalyse.WordAnalyzer;

public class LValParser extends GrammarUnit {
    private final LValNode treeNode;

    public LValParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        if (!checkWord("IDENFR")) {
            handleError(this.getClass(), 0);
        }
        treeNode = new LValNode(word, father);
        while (checkWord("LBRACK")) {
            treeNode.addDimension((new ExpParser(wordAnalyzer, treeNode)).getTreeNode());
            if (!checkWord("RBRACK")) {
                handleError(this.getClass(), 1);
            }
        }
        super.log();
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}