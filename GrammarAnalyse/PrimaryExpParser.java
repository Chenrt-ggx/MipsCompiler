package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.ValueNode;
import SyntaxTree.UnaryExpNode;
import WordAnalyse.WordAnalyzer;

public class PrimaryExpParser extends GrammarUnit {
    public PrimaryExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        readWord();
        if (word != null && word.getType().equals("LPARENT")) {
            wordAnalyzer.pushUp();
            ((UnaryExpNode)father).setUnary(null, (new ExpParser(wordAnalyzer, father)).getTreeNode());
            if (!checkWord("RPARENT")) {
                handleError(this.getClass(), 0);
            }
        }
        else if (word != null && word.getType().equals("INTCON")) {
            wordAnalyzer.backUp();
            ValueNode valNode = new ValueNode(null, father);
            valNode.setValue((new NumberParser(wordAnalyzer)).getValue());
            ((UnaryExpNode)father).setUnary(null, valNode);
        }
        else {
            wordAnalyzer.backUp();
            ((UnaryExpNode)father).setUnary(null, (new LValParser(wordAnalyzer, father)).getTreeNode());
        }
        super.log();
    }

    public PrimaryExpParser(WordAnalyzer wordAnalyzer, TreeNode father, TreeNode preReadNode) {
        super(wordAnalyzer);
        relinkFather(preReadNode, father);
        ((UnaryExpNode)father).setUnary(null, preReadNode);
        super.log();
    }
}