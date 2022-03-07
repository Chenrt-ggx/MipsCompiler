package GrammarAnalyse;

import Global.Pair;
import Global.Settings;
import Global.ErrorInfo;
import Global.LogBuffer;
import java.util.HashSet;
import java.util.HashMap;
import SyntaxTree.TreeNode;
import WordAnalyse.Word.Word;
import java.lang.reflect.Field;
import WordAnalyse.WordAnalyzer;
import java.util.function.Consumer;
import WordAnalyse.UnknownWordFormatException;

public abstract class GrammarUnit {
    protected static final HashSet<String> expBegins;
    protected static final HashMap<Pair<Class<? extends GrammarUnit>, Integer>, Consumer<Word>> handlers;

    static {
        expBegins = new HashSet<>();
        expBegins.add("NOT");
        expBegins.add("PLUS");
        expBegins.add("MINU");
        expBegins.add("LPARENT");
        expBegins.add("INTCON");
        expBegins.add("IDENFR");
        handlers = new HashMap<>();
        handlers.put(new Pair<>(StmtParser.class, 4), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(StmtParser.class, 5), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(StmtParser.class, 6), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(StmtParser.class, 7), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(StmtParser.class, 11), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(StmtParser.class, 14), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(StmtParser.class, 15), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(StmtParser.class, 16), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(VarDeclParser.class, 1), word -> ErrorInfo.push(word.getLine(), "i"));
        handlers.put(new Pair<>(ConstDeclParser.class, 2), word -> ErrorInfo.push(word.getLine(), "i"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(StmtParser.class, 1), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(StmtParser.class, 3), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(StmtParser.class, 10), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(StmtParser.class, 13), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(FuncDefParser.class, 2), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(FuncDefParser.class, 3), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(UnaryExpParser.class, 0), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(UnaryExpParser.class, 1), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(PrimaryExpParser.class, 0), word -> ErrorInfo.push(word.getLine(), "j"));
        handlers.put(new Pair<>(MainFuncDefParser.class, 3), word -> ErrorInfo.push(word.getLine(), "j"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(LValParser.class, 1), word -> ErrorInfo.push(word.getLine(), "k"));
        handlers.put(new Pair<>(VarDefParser.class, 1), word -> ErrorInfo.push(word.getLine(), "k"));
        handlers.put(new Pair<>(ConstDefParser.class, 1), word -> ErrorInfo.push(word.getLine(), "k"));
        handlers.put(new Pair<>(FuncFParamParser.class, 2), word -> ErrorInfo.push(word.getLine(), "k"));
        handlers.put(new Pair<>(FuncFParamParser.class, 3), word -> ErrorInfo.push(word.getLine(), "k"));
    }

    protected void relinkFather(TreeNode treeNode, TreeNode father) {
        try {
            Field field = TreeNode.class.getDeclaredField("father");
            field.setAccessible(true);
            field.set(treeNode, father);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected Word word;
    protected final WordAnalyzer wordAnalyzer;

    public GrammarUnit(WordAnalyzer wordAnalyzer) {
        this.word = null;
        this.wordAnalyzer = wordAnalyzer;
    }

    protected void readWord() {
        while (true) {
            try {
                word = wordAnalyzer.getWord();
                return;
            }
            catch (UnknownWordFormatException e) {
                if (Settings.wordErrorToStderr) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected boolean checkWord(String... expected) {
        readWord();
        if (word != null) {
            for (String i : expected) {
                if (word.getType().equals(i)) {
                    wordAnalyzer.pushUp();
                    return true;
                }
            }
        }
        wordAnalyzer.backUp();
        return false;
    }

    protected void log() {
        if (Settings.grammarAnalyseToFile) {
            LogBuffer.buffer.add("<" + this + ">");
        }
        if (Settings.grammarAnalyseToStdout) {
            String out = "::         <" + this + ">         ::";
            String upAndDown = out.replaceAll(".?", ":").substring(1);
            System.out.println("\n" + upAndDown);
            System.out.println(out);
            System.out.println(upAndDown + "\n");
        }
    }

    protected void handleError(Class<? extends GrammarUnit> srcClass, int place) {
        Pair<Class<? extends GrammarUnit>, Integer> pair = new Pair<>(srcClass, place);
        Consumer<Word> action = handlers.get(pair);
        String append = action == null ? "default error handler" : "specific error handler";
        if (Settings.grammarErrorToStderr) {
            System.err.println("bad word occurred at " + srcClass + ", using " + append);
            System.err.println("reference error line : " + (word == null
                    ? "undetermined" : word.getLine()));
            System.err.print("error place id : " + place + " -> ");
            (new Exception()).printStackTrace();
            System.err.println();
        }
        if (action != null) {
            action.accept(wordAnalyzer.getLastWord());
        }
    }

    @Override
    public String toString() {
        String name = this.getClass().toString();
        return name.substring(name.lastIndexOf('.') + 1, name.length() - 6);
    }
}