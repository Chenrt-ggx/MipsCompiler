package SyntaxTree;

import java.util.HashSet;
import java.util.ArrayList;
import WordAnalyse.Word.Word;
import IntermediateCode.Operands.Function;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.ParamVariable;
import IntermediateCode.InterElement.AbstractElement;

public class FuncDefNode extends TreeNode {
    public static final boolean INT_TYPE = true;
    public static final boolean VOID_TYPE = false;

    private int size;
    private boolean type;
    private BlockNode block;
    private Function functionDef;
    private final boolean isMain;
    private final ArrayList<FParamNode> params;
    private final HashSet<FuncDefNode> callFunction;

    public FuncDefNode(Word mainWord, TreeNode father, boolean isMain) {
        super(mainWord, father);
        type = false;
        block = null;
        functionDef = null;
        this.isMain = isMain;
        params = new ArrayList<>();
        callFunction = new HashSet<>();
        if (this.lastSymbolTable.queryDefine(mainWord.getSourceWord()) != null) {
            handleError(this.getClass(), 0);
        }
    }

    public void setBlock(TreeNode treeNode) {
        block = (BlockNode)treeNode;
        selfConnectionCheck(treeNode);
    }

    public void addParam(TreeNode treeNode) {
        ((FParamNode)treeNode).setPlace(params.size());
        params.add((FParamNode)treeNode);
        selfConnectionCheck(treeNode);
    }

    protected void addCaller(FuncDefNode caller) {
        callFunction.add(caller);
    }

    protected int getSize() {
        return size;
    }

    protected boolean getType() {
        return type;
    }

    protected boolean isMain() {
        return isMain;
    }

    protected BlockNode getBlocks() {
        return block;
    }

    public void setType(boolean which) {
        this.type = which;
    }

    protected void flushParams(IntermediateBuilder builder) {
        params.forEach(i -> i.flush(builder));
    }

    protected void searchCallers(HashSet<FuncDefNode> unique) {
        callFunction.stream().filter(i -> !unique.contains(i)).forEach(i -> {
            unique.add(i);
            i.searchCallers(unique);
        });
    }

    protected ArrayList<FParamNode> getParams() {
        return params;
    }

    protected Function getFunctionDef() {
        return functionDef;
    }

    @Override
    public void calculate() {
        block.calculate();
        params.forEach(TreeNode::calculate);
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        params.forEach(i -> i.toIntermediate(builder));
        ArrayList<ParamVariable> paramBuilder = new ArrayList<>(params.size());
        params.forEach(i -> paramBuilder.add((ParamVariable)i.getDstAbstractVariable()));
        functionDef = builder.putFunction(mainWord.getSourceWord(), paramBuilder, isMain);
        builder.putIntermediate(AbstractElement.getTagElement(functionDef), layer);
        block.toIntermediate(builder);
        size = builder.getCurrentFunctionSize();
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append('\n');
        builder.append(type == INT_TYPE ? "int" : "void").append(' ');
        builder.append(mainWord.getSourceWord()).append('(');
        if (!params.isEmpty()) {
            params.get(0).display(builder);
            for (int i = 1; i < params.size(); i++) {
                builder.append(',').append(' ');
                params.get(i).display(builder);
            }
        }
        builder.append(')').append(' ');
        block.display(builder);
    }
}