package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.BlockNode;
import WordAnalyse.WordAnalyzer;

public class BlockParser extends GrammarUnit {
    private final BlockNode treeNode;

    public BlockParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new BlockNode(null, father);
        if (!checkWord("LBRACE")) {
            handleError(this.getClass(), 0);
        }
        while (true) {
            readWord();
            if (word != null && word.getType().equals("RBRACE")) {
                break;
            }
            else if (word != null && word.getType().equals("CONSTTK")) {
                wordAnalyzer.backUp();
                new ConstDeclParser(wordAnalyzer, treeNode);
            }
            else if (word != null && word.getType().equals("INTTK")) {
                wordAnalyzer.backUp();
                new VarDeclParser(wordAnalyzer, treeNode);
            }
            else {
                wordAnalyzer.backUp();
                treeNode.addStmt((new StmtParser(wordAnalyzer, treeNode)).getTreeNode());
            }
        }
        treeNode.selfCheck(word);
        wordAnalyzer.pushUp();
        super.log();
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}