package GrammarAnalyse;

import SyntaxTree.TreeNode;
import WordAnalyse.Word.Word;
import SyntaxTree.FuncCallNode;
import SyntaxTree.UnaryExpNode;
import WordAnalyse.WordAnalyzer;

public class UnaryExpParser extends GrammarUnit {
    private final UnaryExpNode treeNode;

    public UnaryExpParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = new UnaryExpNode(null, father);
        readWord();
        if (word != null && (word.getType().equals("NOT") ||
                word.getType().equals("PLUS") || word.getType().equals("MINU"))) {
            wordAnalyzer.backUp();
            treeNode.setUnary(new UnaryOpParser(wordAnalyzer).getType(),
                    (new UnaryExpParser(wordAnalyzer, treeNode)).getTreeNode());
        }
        else if (word != null && (word.getType().equals("IDENFR"))) {
            Word tempWord = word;
            if (!checkWord("LPARENT")) {
                new PrimaryExpParser(wordAnalyzer, treeNode);
            }
            else {
                FuncCallNode funcNode = new FuncCallNode(tempWord, treeNode);
                readWord();
                if (expBegins.contains(word.getType())) {
                    wordAnalyzer.backUp();
                    new FuncRParamsParser(wordAnalyzer, funcNode);
                    if (!checkWord("RPARENT")) {
                        handleError(this.getClass(), 0);
                    }
                }
                else if (word != null && !word.getType().equals("RPARENT")) {
                    wordAnalyzer.backUp();
                    handleError(this.getClass(), 1);
                }
                else {
                    wordAnalyzer.pushUp();
                }
                funcNode.selfCheck();
                treeNode.setUnary(null, funcNode);
            }
        }
        else {
            wordAnalyzer.backUp();
            new PrimaryExpParser(wordAnalyzer, treeNode);
        }
        super.log();
    }

    public UnaryExpParser(WordAnalyzer wordAnalyzer, TreeNode father, TreeNode preReadNode) {
        super(wordAnalyzer);
        treeNode = new UnaryExpNode(null, father);
        new PrimaryExpParser(wordAnalyzer, treeNode, preReadNode);
        super.log();
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}