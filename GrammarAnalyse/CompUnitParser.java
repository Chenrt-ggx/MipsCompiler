package GrammarAnalyse;

import SyntaxTree.RootNode;
import WordAnalyse.WordAnalyzer;

public class CompUnitParser extends GrammarUnit {
    private final RootNode rootNode;

    public CompUnitParser(WordAnalyzer wordAnalyzer) {
        super(wordAnalyzer);
        rootNode = new RootNode(null, null);
        while (true) {
            readWord();
            if (word != null && word.getType().equals("CONSTTK")) {
                wordAnalyzer.backUp();
                new ConstDeclParser(wordAnalyzer, rootNode);
                continue;
            }
            else if (word != null && word.getType().equals("INTTK")) {
                readWord();
                if (word != null && word.getType().equals("IDENFR")) {
                    readWord();
                    if (word == null || !word.getType().equals("LPARENT")) {
                        wordAnalyzer.backUp();
                        new VarDeclParser(wordAnalyzer, rootNode);
                        continue;
                    }
                }
            }
            wordAnalyzer.backUp();
            break;
        }
        while (true) {
            readWord();
            if (word != null && word.getType().equals("VOIDTK")) {
                wordAnalyzer.backUp();
                new FuncDefParser(wordAnalyzer, rootNode);
                continue;
            }
            else if (word != null && word.getType().equals("INTTK")) {
                readWord();
                if (word != null && word.getType().equals("IDENFR")) {
                    wordAnalyzer.backUp();
                    new FuncDefParser(wordAnalyzer, rootNode);
                    continue;
                }
            }
            wordAnalyzer.backUp();
            break;
        }
        new MainFuncDefParser(wordAnalyzer, rootNode);
        super.log();
    }

    public RootNode getTreeNode() {
        return rootNode;
    }
}