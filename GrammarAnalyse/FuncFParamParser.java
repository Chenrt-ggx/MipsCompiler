package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.FParamNode;
import SyntaxTree.FuncDefNode;
import WordAnalyse.WordAnalyzer;

public class FuncFParamParser extends GrammarUnit {
    public FuncFParamParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        if (!checkWord("INTTK")) {
            handleError(this.getClass(), 0);
        }
        if (!checkWord("IDENFR")) {
            handleError(this.getClass(), 1);
        }
        FParamNode treeNode = new FParamNode(word, father);
        if (checkWord("LBRACK")) {
            if (!checkWord("RBRACK")) {
                handleError(this.getClass(), 2);
            }
            treeNode.addDimension(null);
            while (checkWord("LBRACK")) {
                treeNode.addDimension((new ConstExpParser(wordAnalyzer, treeNode)).getTreeNode());
                if (!checkWord("RBRACK")) {
                    handleError(this.getClass(), 3);
                }
            }
        }
        ((FuncDefNode)father).addParam(treeNode);
        super.log();
    }
}