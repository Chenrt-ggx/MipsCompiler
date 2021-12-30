package GrammarAnalyse;

import SyntaxTree.TreeNode;
import SyntaxTree.LoopNode;
import SyntaxTree.BreakNode;
import SyntaxTree.PrintNode;
import SyntaxTree.ReturnNode;
import SyntaxTree.AssignNode;
import WordAnalyse.Word.Word;
import SyntaxTree.ContinueNode;
import SyntaxTree.ConditionNode;
import WordAnalyse.WordAnalyzer;

public class StmtParser extends GrammarUnit {
    private TreeNode treeNode;
    private final TreeNode father;

    public StmtParser(WordAnalyzer wordAnalyzer, TreeNode father) {
        super(wordAnalyzer);
        treeNode = null;
        this.father = father;
        readWord();
        if (word != null && word.getType().equals("SEMICN")) {
            semSolver();
        }
        else if (word != null && word.getType().equals("LBRACE")) {
            blockSolver();
        }
        else if (word != null && word.getType().equals("IFTK")) {
            ifSolver();
        }
        else if (word != null && word.getType().equals("WHILETK")) {
            whileSolver();
        }
        else if (word != null && word.getType().equals("BREAKTK")) {
            breakSolver();
        }
        else if (word != null && word.getType().equals("CONTINUETK")) {
            continueSolver();
        }
        else if (word != null && word.getType().equals("RETURNTK")) {
            returnSolver();
        }
        else if (word != null && word.getType().equals("PRINTFTK")) {
            printfSolver();
        }
        else if (word != null && word.getType().equals("IDENFR")) {
            lValSolver();
        }
        else {
            expSolver();
        }
    }

    private void semSolver() {
        wordAnalyzer.pushUp();
        super.log();
    }

    private void blockSolver() {
        wordAnalyzer.backUp();
        treeNode = (new BlockParser(wordAnalyzer, father)).getTreeNode();
        super.log();
    }

    private void ifSolver() {
        wordAnalyzer.pushUp();
        treeNode = new ConditionNode(word, father);
        if (!checkWord("LPARENT")) {
            handleError(this.getClass(), 0);
        }
        ((ConditionNode)treeNode).addCondition((new CondParser(wordAnalyzer, treeNode)).getTreeNode());
        if (!checkWord("RPARENT")) {
            handleError(this.getClass(), 1);
        }
        ((ConditionNode)treeNode).setThenStmt((new StmtParser(wordAnalyzer, treeNode)).getTreeNode());
        if (checkWord("ELSETK")) {
            ((ConditionNode)treeNode).setElseStmt((new StmtParser(wordAnalyzer, treeNode)).getTreeNode());
        }
        super.log();
    }

    private void whileSolver() {
        wordAnalyzer.pushUp();
        treeNode = new LoopNode(word, father);
        if (!checkWord("LPARENT")) {
            handleError(this.getClass(), 2);
        }
        ((LoopNode)treeNode).addCondition((new CondParser(wordAnalyzer, treeNode)).getTreeNode());
        if (!checkWord("RPARENT")) {
            handleError(this.getClass(), 3);
        }
        ((LoopNode)treeNode).addStmt((new StmtParser(wordAnalyzer, treeNode)).getTreeNode());
        super.log();
    }

    private void breakSolver() {
        wordAnalyzer.pushUp();
        treeNode = new BreakNode(word, father);
        if (!checkWord("SEMICN")) {
            handleError(this.getClass(), 4);
        }
        super.log();
    }

    private void continueSolver() {
        wordAnalyzer.pushUp();
        treeNode = new ContinueNode(word, father);
        if (!checkWord("SEMICN")) {
            handleError(this.getClass(), 5);
        }
        super.log();
    }

    private void returnSolver() {
        wordAnalyzer.pushUp();
        treeNode = new ReturnNode(word, father);
        readWord();
        if (expBegins.contains(word.getType())) {
            wordAnalyzer.backUp();
            ((ReturnNode)treeNode).setExp((new ExpParser(wordAnalyzer, treeNode)).getTreeNode());
            if (!checkWord("SEMICN")) {
                handleError(this.getClass(), 6);
            }
        }
        else if (word != null && !word.getType().equals("SEMICN")) {
            wordAnalyzer.backUp();
            handleError(this.getClass(), 7);
        }
        else {
            wordAnalyzer.pushUp();
        }
        ((ReturnNode)treeNode).selfCheck();
        super.log();
    }

    private void printfSolver() {
        wordAnalyzer.pushUp();
        Word printWord = word;
        if (!checkWord("LPARENT")) {
            handleError(this.getClass(), 8);
        }
        if (!checkWord("STRCON")) {
            handleError(this.getClass(), 9);
        }
        treeNode = new PrintNode(word, father, printWord);
        while (checkWord("COMMA")) {
            ((PrintNode)treeNode).addExp((new ExpParser(wordAnalyzer, treeNode)).getTreeNode());
        }
        if (!checkWord("RPARENT")) {
            handleError(this.getClass(), 10);
        }
        if (!checkWord("SEMICN")) {
            handleError(this.getClass(), 11);
        }
        ((PrintNode)treeNode).selfCheck();
        super.log();
    }

    private void lValSolver() {
        readWord();
        if (word != null && word.getType().equals("LPARENT")) {
            expSolver();
            return;
        }
        wordAnalyzer.backUp();
        LValParser parser = new LValParser(wordAnalyzer, father);
        readWord();
        if (word != null && word.getType().equals("ASSIGN")) {
            wordAnalyzer.pushUp();
            treeNode = new AssignNode(word, father);
            relinkFather(parser.getTreeNode(), treeNode);
            ((AssignNode)treeNode).setLVal(parser.getTreeNode());
            if (!checkWord("GETINTTK")) {
                ((AssignNode)treeNode).setRightExp((new ExpParser(wordAnalyzer, treeNode)).getTreeNode());
            }
            else {
                if (!checkWord("LPARENT")) {
                    handleError(this.getClass(), 12);
                }
                if (!checkWord("RPARENT")) {
                    handleError(this.getClass(), 13);
                }
            }
            if (!checkWord("SEMICN")) {
                handleError(this.getClass(), 14);
            }
            super.log();
            return;
        }
        wordAnalyzer.backUp();
        expSolver(parser.getTreeNode());
    }

    private void expSolver() {
        wordAnalyzer.backUp();
        treeNode = (new ExpParser(wordAnalyzer, father)).getTreeNode();
        if (!checkWord("SEMICN")) {
            handleError(this.getClass(), 15);
        }
        super.log();
    }

    private void expSolver(TreeNode perReadNode) {
        wordAnalyzer.backUp();
        treeNode = (new ExpParser(wordAnalyzer, father, perReadNode)).getTreeNode();
        if (!checkWord("SEMICN")) {
            handleError(this.getClass(), 16);
        }
        super.log();
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}