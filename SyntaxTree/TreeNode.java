package SyntaxTree;

import Global.Pair;
import Global.Settings;
import Global.ErrorInfo;
import java.util.HashMap;
import java.util.LinkedList;
import WordAnalyse.Word.Word;
import java.util.function.Consumer;
import IntermediateCode.Operands.TagString;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;

public abstract class TreeNode {
    protected static final class ExpressionInfo {
        private final int value;
        private final boolean pure;
        private final boolean valid;

        protected ExpressionInfo(int value, boolean pure, boolean valid) {
            this.pure = pure;
            this.valid = valid;
            this.value = value;
        }

        protected int getValue() {
            return value;
        }

        protected boolean isPure() {
            return pure;
        }

        protected boolean isValid() {
            return valid;
        }
    }

    protected static int layer;
    protected static LinkedList<Pair<TagString, AbstractVariable>> inlineStack;
    protected static final HashMap<Pair<Class<? extends TreeNode>, Integer>, Consumer<Word>> handlers;

    static {
        handlers = new HashMap<>();
        handlers.put(new Pair<>(PrintNode.class, 0), word -> ErrorInfo.push(word.getLine(), "a"));
        handlers.put(new Pair<>(PrintNode.class, 1), word -> ErrorInfo.push(word.getLine(), "a"));
        handlers.put(new Pair<>(PrintNode.class, 2), word -> ErrorInfo.push(word.getLine(), "a"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(BlockNode.class, 0), word -> ErrorInfo.push(word.getLine(), "b"));
        handlers.put(new Pair<>(VarDefNode.class, 0), word -> ErrorInfo.push(word.getLine(), "b"));
        handlers.put(new Pair<>(FuncDefNode.class, 0), word -> ErrorInfo.push(word.getLine(), "b"));
        handlers.put(new Pair<>(ConstDefNode.class, 0), word -> ErrorInfo.push(word.getLine(), "b"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(LValNode.class, 0), word -> ErrorInfo.push(word.getLine(), "c"));
        handlers.put(new Pair<>(LValNode.class, 1), word -> ErrorInfo.push(word.getLine(), "c"));
        handlers.put(new Pair<>(FuncCallNode.class, 0), word -> ErrorInfo.push(word.getLine(), "c"));
        handlers.put(new Pair<>(FuncCallNode.class, 1), word -> ErrorInfo.push(word.getLine(), "c"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(FuncCallNode.class, 2), word -> ErrorInfo.push(word.getLine(), "d"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(FuncCallNode.class, 3), word -> ErrorInfo.push(word.getLine(), "e"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(ReturnNode.class, 1), word -> ErrorInfo.push(word.getLine(), "f"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(BlockNode.class, 3), word -> ErrorInfo.push(word.getLine(), "g"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(AssignNode.class, 0), word -> ErrorInfo.push(word.getLine(), "h"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(PrintNode.class, 3), word -> ErrorInfo.push(word.getLine(), "l"));
        // --------------------------------------------------------------------------------
        handlers.put(new Pair<>(BreakNode.class, 0), word -> ErrorInfo.push(word.getLine(), "m"));
        handlers.put(new Pair<>(ContinueNode.class, 0), word -> ErrorInfo.push(word.getLine(), "m"));
    }

    protected static void indentLayer(StringBuilder builder) {
        for (int i = 0; i < layer << 2; i++) {
            builder.append(' ');
        }
    }

    protected Word mainWord;
    protected final TreeNode father;
    protected final SymbolTable lastSymbolTable;

    public TreeNode(Word mainWord, TreeNode father) {
        this.father = father;
        this.mainWord = mainWord;
        lastSymbolTable = father == null ? null : father instanceof SymbolTable ?
                (SymbolTable)father : father.lastSymbolTable;
    }

    protected void selfConnectionCheck(TreeNode treeNode) {
        if (Settings.syntaxTreeConnectionSelfCheck) {
            if (treeNode.father != this) {
                System.err.println("tree node connection check failed");
                (new Exception()).printStackTrace();
                System.exit(0);
            }
        }
    }

    protected void handleError(Class<? extends TreeNode> srcClass, int place, Word aim) {
        Pair<Class<? extends TreeNode>, Integer> pair = new Pair<>(srcClass, place);
        Consumer<Word> action = handlers.get(pair);
        String append = action == null ? "default error handler" : "specific error handler";
        if (Settings.syntaxTreeErrorToStderr) {
            System.err.println("bad grammar occurred at " + srcClass + ", using " + append);
            System.err.println("reference error line : " + (mainWord == null
                    ? "undetermined" : mainWord.getLine()));
            System.err.print("error place id : " + place + " -> ");
            (new Exception()).printStackTrace();
            System.err.println();
        }
        if (action != null) {
            action.accept(aim);
        }
    }

    protected void handleError(Class<? extends TreeNode> srcClass, int place) {
        handleError(srcClass, place, mainWord);
    }

    public abstract void calculate();

    public abstract void toIntermediate(IntermediateBuilder builder);

    public abstract void display(StringBuilder builder);
}