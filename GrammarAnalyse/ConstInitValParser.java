package GrammarAnalyse;

import SyntaxTree.TreeNode;
import java.util.LinkedList;
import SyntaxTree.ConstDefNode;
import WordAnalyse.WordAnalyzer;

public class ConstInitValParser extends GrammarUnit {
    public ConstInitValParser(WordAnalyzer wordAnalyzer, TreeNode father, LinkedList<Integer> level) {
        super(wordAnalyzer);
        if (!checkWord("LBRACE")) {
            ((ConstDefNode)father).addInit((new ConstExpParser(wordAnalyzer, father)).getTreeNode(), level);
        }
        else if (!checkWord("RBRACE")) {
            int count = 0;
            level.add(count);
            new ConstInitValParser(wordAnalyzer, father, level);
            level.removeLast();
            while (checkWord("COMMA")) {
                level.add(++count);
                new ConstInitValParser(wordAnalyzer, father, level);
                level.removeLast();
            }
            if (!checkWord("RBRACE")) {
                handleError(this.getClass(), 0);
            }
        }
        super.log();
    }
}