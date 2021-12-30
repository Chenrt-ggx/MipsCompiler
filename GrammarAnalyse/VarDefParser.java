package GrammarAnalyse;

import SyntaxTree.TreeNode;
import java.util.LinkedList;
import SyntaxTree.VarDefNode;
import SyntaxTree.SymbolTable;
import WordAnalyse.WordAnalyzer;

public class VarDefParser extends GrammarUnit {
    public VarDefParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        if (!checkWord("IDENFR")) {
            handleError(this.getClass(), 0);
        }
        VarDefNode treeNode = new VarDefNode(word, father);
        while (checkWord("LBRACK")) {
            treeNode.addDimension((new ConstExpParser(wordAnalyzer, treeNode)).getTreeNode());
            if (!checkWord("RBRACK")) {
                handleError(this.getClass(), 1);
            }
        }
        if (checkWord("ASSIGN")) {
            new InitValParser(wordAnalyzer, treeNode, new LinkedList<>());
        }
        ((SymbolTable)father).addVarDef(treeNode);
        super.log();
    }
}