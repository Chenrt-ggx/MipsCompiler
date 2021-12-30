package SyntaxTree;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class VarDefNode extends TreeNode implements AbstractVarDef {
    private final ArrayList<Integer> dimensions;
    private final ArrayList<AddExpNode> initList;
    private AbstractVariable dstAbstractVariable;
    private final HashMap<LinkedList<Integer>, AddExpNode> init;

    public VarDefNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        init = new HashMap<>();
        initList = new ArrayList<>();
        dimensions = new ArrayList<>();
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
        selfConnectionCheck(treeNode);
        initList.add((AddExpNode)treeNode);
        init.put(new LinkedList<>(level), (AddExpNode)treeNode);
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
        if (init.containsKey(path)) {
            return init.get(path).getExpressionInfo().getValue();
        }
        else {
            return 0;
        }
    }

    @Override
    public void calculate() {
        initList.forEach(TreeNode::calculate);
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (dimensions.size() == 0) {
            dstAbstractVariable = builder.putVariable(true);
            LinkedList<Integer> query = new LinkedList<>();
            AddExpNode initExp = init.get(query);
            if (initExp == null) {
                builder.putIntermediate(AbstractElement.getDeclElement(dstAbstractVariable), layer);
                return;
            }
            if (!(lastSymbolTable instanceof RootNode)) {
                initExp.toIntermediate(builder);
            }
            builder.putIntermediate(AbstractElement.getAddElement(dstAbstractVariable,
                    lastSymbolTable instanceof RootNode ? builder.putIntConst(initExp.getExpressionInfo()
                    .getValue()) : initExp.getDstAbstractVariable(), builder.putIntConst(0)), layer);
        }
        else {
            dstAbstractVariable = builder.putVariable(true);
            AbstractVariable size = builder.putIntConst(dimensions.stream()
                    .mapToInt(Integer::intValue).reduce(1, (x, y) -> x * y));
            builder.putIntermediate(AbstractElement.getAllocElement(dstAbstractVariable, size), layer);
            if (!initList.isEmpty()) {
                if (!(lastSymbolTable instanceof RootNode)) {
                    initList.forEach(i -> i.toIntermediate(builder));
                }
                AbstractVariable initAbstractVariable = builder.putVariable();
                for (int i = 0; i < initList.size(); i++) {
                    builder.putIntermediate(i == 0 ?
                            AbstractElement.getAddElement(initAbstractVariable, dstAbstractVariable
                                    , builder.putIntConst(0)) :
                            AbstractElement.getAddElement(initAbstractVariable, initAbstractVariable,
                                    builder.putIntConst(4)), layer);
                    builder.putIntermediate(AbstractElement.getStoreElement(lastSymbolTable instanceof RootNode ?
                            builder.putIntConst(initList.get(i).getExpressionInfo().getValue()) :
                            initList.get(i).getDstAbstractVariable(), initAbstractVariable), layer);
                }
            }
        }
    }

    private void displayInit(LinkedList<Integer> cur, int level, StringBuilder builder) {
        if (level == dimensions.size()) {
            AddExpNode node = init.get(cur);
            if (node == null) {
                builder.append(0);
            }
            else {
                node.display(builder);
            }
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
        builder.append("int ").append(mainWord.getSourceWord());
        dimensions.forEach(i -> builder.append('[').append(i).append(']'));
        if (!init.isEmpty() || ((TreeNode)this.lastSymbolTable).lastSymbolTable == null) {
            builder.append(' ').append('=').append(' ');
            displayInit(new LinkedList<>(), 0, builder);
        }
        builder.append(';').append('\n');
    }
}