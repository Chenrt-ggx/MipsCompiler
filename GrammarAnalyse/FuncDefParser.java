package GrammarAnalyse;

import SyntaxTree.RootNode;
import SyntaxTree.TreeNode;
import SyntaxTree.FuncDefNode;
import WordAnalyse.WordAnalyzer;

public class FuncDefParser extends GrammarUnit {
    public FuncDefParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        boolean type = (new FuncTypeParser(wordAnalyzer)).getType().equals("VOIDTK") ?
                FuncDefNode.VOID_TYPE : FuncDefNode.INT_TYPE;
        if (!checkWord("IDENFR")) {
            handleError(this.getClass(), 0);
        }
        FuncDefNode treeNode = new FuncDefNode(word, father, false);
        treeNode.setType(type);
        if (!checkWord("LPARENT")) {
            handleError(this.getClass(), 1);
        }
        readWord();
        if (word != null && word.getType().equals("INTTK")) {
            wordAnalyzer.backUp();
            new FuncFParamsParser(wordAnalyzer, treeNode);
            if (!checkWord("RPARENT")) {
                handleError(this.getClass(), 2);
            }
        }
        else if (word != null && !word.getType().equals("RPARENT")) {
            wordAnalyzer.backUp();
            handleError(this.getClass(), 3);
        }
        else {
            wordAnalyzer.pushUp();
        }
        ((RootNode)father).addFuncDef(treeNode);
        treeNode.setBlock((new BlockParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
    }
}