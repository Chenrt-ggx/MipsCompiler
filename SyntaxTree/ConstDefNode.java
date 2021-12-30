package SyntaxTree;

import Global.Settings;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class ConstDefNode extends TreeNode implements AbstractVarDef {
    private boolean dumpedInRootNode;
    private final ArrayList<Integer> initList;
    private final ArrayList<Integer> dimensions;
    private AbstractVariable dstAbstractVariable;
    private final HashMap<LinkedList<Integer>, Integer> init;

    public ConstDefNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        init = new HashMap<>();
        initList = new ArrayList<>();
        dimensions = new ArrayList<>();
        dumpedInRootNode = false;
        if (this.lastSymbolTable.queryDefine(mainWord.getSourceWord()) != null) {
            handleError(this.getClass(), 0);
        }
    }

    @Override
    public void addDimension(TreeNode treeNode) {
        ExpressionInfo val = ((AddExpNode)treeNode).getExpressionInfo();
        if (val.isValid() && val.getValue() > 0) {
            dimensions.add(val.getValue());
        }
        else {
            handleError(this.getClass(), 1);
        }
    }

    @Override
    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    public void addInit(TreeNode treeNode, LinkedList<Integer> level) {
        if (level.size() != dimensions.size()) {
            handleError(this.getClass(), 2);
            return;
        }
        int count = 0;
        for (int i : level) {
            if (i >= dimensions.get(count++)) {
                handleError(this.getClass(), 3);
                return;
            }
        }
        ExpressionInfo val = ((AddExpNode)treeNode).getExpressionInfo();
        if (val.isValid()) {
            initList.add(val.getValue());
            init.put(new LinkedList<>(level), val.getValue());
        }
        else {
            handleError(this.getClass(), 4);
        }
    }

    protected void setDumpedInRootNode() {
        this.dumpedInRootNode = true;
    }

    @Override
    public AbstractVariable getDstAbstractVariable() {
        return dstAbstractVariable;
    }

    @Override
    public int offsetOf(int index) {
        int ans = 1;
        for (int i = index + 1; i < dimensions.size(); i++) {
            ans *= dimensions.get(i);
        }
        return ans;
    }

    protected int getValue(LinkedList<Integer> path) {
        return init.getOrDefault(path, 0);
    }

    @Override
    public void calculate() {
        TreeNode rootNode = null;
        for (TreeNode i = this.father; i != null; i = i.father) {
            rootNode = i;
        }
        assert rootNode != null;
        ((RootNode)rootNode).addConstDefInFunction(this);
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.moveConstInFunctionToGlobal && dumpedInRootNode) {
            return;
        }
        if (dimensions.size() == 0) {
            LinkedList<Integer> query = new LinkedList<>();
            dstAbstractVariable = builder.putIntConst(init.get(query));
        }
        else {
            dstAbstractVariable = builder.putVariable(true);
            AbstractVariable size = builder.putIntConst(dimensions.stream()
                    .mapToInt(Integer::intValue).reduce(1, (x, y) -> x * y));
            builder.putIntermediate(AbstractElement.getAllocElement(dstAbstractVariable, size), layer);
            AbstractVariable initAbstractVariable = builder.putVariable();
            for (int i = 0; i < initList.size(); i++) {
                builder.putIntermediate(i == 0 ?
                        AbstractElement.getAddElement(initAbstractVariable, dstAbstractVariable
                                , builder.putIntConst(0)) :
                        AbstractElement.getAddElement(initAbstractVariable, initAbstractVariable,
                                builder.putIntConst(4)), layer);
                builder.putIntermediate(AbstractElement.getStoreElement(
                        builder.putIntConst(initList.get(i)), initAbstractVariable), layer);
            }
        }
    }

    private void displayInit(LinkedList<Integer> cur, int level, StringBuilder builder) {
        if (level == dimensions.size()) {
            builder.append(init.getOrDefault(cur, 0));
            return;
        }
        builder.append('{');
        cur.add(0);
        displayInit(cur, level + 1, builder);
        cur.removeLast();
        for (int i = 1; i < dimensions.get(level); i++) {
            builder.append(',').append(' ');
            cur.add(i);
            displayInit(cur, level + 1, builder);
            cur.removeLast();
        }
        builder.append('}');
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append("const int ").append(mainWord.getSourceWord());
        dimensions.forEach(i -> builder.append('[').append(i).append(']'));
        builder.append(' ').append('=').append(' ');
        displayInit(new LinkedList<>(), 0, builder);
        builder.append(';').append('\n');
    }
}