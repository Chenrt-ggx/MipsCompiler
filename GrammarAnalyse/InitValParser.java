package GrammarAnalyse;

import SyntaxTree.TreeNode;
import java.util.LinkedList;
import SyntaxTree.VarDefNode;
import WordAnalyse.WordAnalyzer;

public class InitValParser extends GrammarUnit {
    public InitValParser(WordAnalyzer wordAnalyzer, TreeNode father, LinkedList<Integer> level) {
        super(wordAnalyzer);
        if (!checkWord("LBRACE")) {
            ((VarDefNode)father).addInit((new ExpParser(wordAnalyzer, father)).getTreeNode(), level);
        }
        else if (!checkWord("RBRACE")) {
            int count = 0;
            level.add(count);
            new InitValParser(wordAnalyzer, father, level);
            level.removeLast();
            while (checkWord("COMMA")) {
                level.add(++count);
                new InitValParser(wordAnalyzer, father, level);
                level.removeLast();
            }
            if (!checkWord("RBRACE")) {
                handleError(this.getClass(), 0);
            }
        }
        super.log();
    }
}