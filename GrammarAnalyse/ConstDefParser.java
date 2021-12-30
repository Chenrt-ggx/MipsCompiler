package GrammarAnalyse;

import SyntaxTree.TreeNode;
import java.util.LinkedList;
import SyntaxTree.SymbolTable;
import SyntaxTree.ConstDefNode;
import WordAnalyse.WordAnalyzer;

public class ConstDefParser extends GrammarUnit {
    public ConstDefParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        if (!checkWord("IDENFR")) {
            handleError(this.getClass(), 0);
        }
        ConstDefNode treeNode = new ConstDefNode(word, father);
        while (checkWord("LBRACK")) {
            treeNode.addDimension((new ConstExpParser(wordAnalyzer, treeNode)).getTreeNode());
            if (!checkWord("RBRACK")) {
                handleError(this.getClass(), 1);
            }
        }
        if (!checkWord("ASSIGN")) {
            handleError(this.getClass(), 2);
        }
        new ConstInitValParser(wordAnalyzer, treeNode, new LinkedList<>());
        ((SymbolTable)father).addConstDef(treeNode);
        super.log();
    }
}