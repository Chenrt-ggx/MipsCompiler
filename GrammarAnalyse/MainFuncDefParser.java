package GrammarAnalyse;

import SyntaxTree.RootNode;
import SyntaxTree.TreeNode;
import SyntaxTree.FuncDefNode;
import WordAnalyse.WordAnalyzer;

public class MainFuncDefParser extends GrammarUnit {
    public MainFuncDefParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        if (!checkWord("INTTK")) {
            handleError(this.getClass(), 0);
        }
        if (!checkWord("MAINTK")) {
            handleError(this.getClass(), 1);
        }
        FuncDefNode treeNode = new FuncDefNode(word, father, true);
        treeNode.setType(FuncDefNode.INT_TYPE);
        if (!checkWord("LPARENT")) {
            handleError(this.getClass(), 2);
        }
        if (!checkWord("RPARENT")) {
            handleError(this.getClass(), 3);
        }
        ((RootNode)father).addFuncDef(treeNode);
        treeNode.setBlock((new BlockParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
    }
}