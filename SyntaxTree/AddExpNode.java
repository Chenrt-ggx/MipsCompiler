package SyntaxTree;

import Global.Pair;
import Global.Settings;
import java.util.ArrayList;
import WordAnalyse.Word.Word;
import java.util.stream.Collectors;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class AddExpNode extends TreeNode {
    private ExpressionInfo expressionInfo;
    private AbstractVariable dstAbstractVariable;
    private final ArrayList<Pair<MulExpNode, String>> children;

    public AddExpNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        children = new ArrayList<>();
    }

    public void addMul(String sign, TreeNode treeNode) {
        if (!children.isEmpty() && sign == null) {
            handleError(this.getClass(), 0);
        }
        children.add(new Pair<>((MulExpNode)treeNode, sign));
        selfConnectionCheck(treeNode);
    }

    protected ExpressionInfo getExpressionInfo() {
        calculate();
        return expressionInfo;
    }

    protected ArrayList<Integer> getDimensions() {
        if (children.size() > 1) {
            return new ArrayList<>();
        }
        return children.get(0).getKey().getDimensions();
    }

    protected AbstractVariable getDstAbstractVariable() {
        return dstAbstractVariable;
    }

    protected void makePure(IntermediateBuilder builder) {
        children.stream().filter(i -> !i.getKey().getExpressionInfo()
                .isPure()).forEach(i -> i.getKey().makePure(builder));
    }

    @Override
    public void calculate() {
        if (expressionInfo == null) {
            children.forEach(i -> i.getKey().calculate());
            boolean isPure = children.get(0).getKey().getExpressionInfo().isPure();
            boolean isValid = children.get(0).getKey().getExpressionInfo().isValid();
            int value = children.get(0).getKey().getExpressionInfo().getValue();
            for (int i = 1; i < children.size(); i++) {
                ExpressionInfo ans = children.get(i).getKey().getExpressionInfo();
                isPure &= ans.isPure();
                isValid &= ans.isValid();
                value = children.get(i).getValue().equals("PLUS") ? value + ans.getValue() :
                        value - ans.getValue();
            }
            expressionInfo = new ExpressionInfo(value, isPure, isValid);
        }
    }

    private boolean childrenListToIntermediate(ArrayList<Pair<MulExpNode, String>> childrenList
            , IntermediateBuilder builder) {
        childrenList.get(0).getKey().toIntermediate(builder);
        dstAbstractVariable = childrenList.get(0).getKey().getDstAbstractVariable();
        for (int i = 1; i < childrenList.size(); i++) {
            childrenList.get(i).getKey().toIntermediate(builder);
            AbstractVariable nextDst = builder.putVariable();
            builder.putIntermediate(childrenList.get(i).getValue().equals("PLUS") ?
                    AbstractElement.getAddElement(nextDst, dstAbstractVariable,
                            childrenList.get(i).getKey().getDstAbstractVariable()) :
                    AbstractElement.getSubElement(nextDst, dstAbstractVariable,
                            childrenList.get(i).getKey().getDstAbstractVariable()), layer);
            dstAbstractVariable = nextDst;
        }
        return childrenList.get(0).getValue() != null && childrenList.get(0).getValue().equals("MINU");
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.syntaxTreeExpressionOptimize && expressionInfo.isValid()) {
            makePure(builder);
            dstAbstractVariable = builder.putIntConst(expressionInfo.getValue());
        }
        else if (Settings.syntaxTreeExpressionOptimize) {
            int value = 0;
            if (children.get(0).getKey().getExpressionInfo().isValid()) {
                children.get(0).getKey().makePure(builder);
                value = children.get(0).getKey().getExpressionInfo().getValue();
            }
            for (int i = 1; i < children.size(); i++) {
                ExpressionInfo info = children.get(i).getKey().getExpressionInfo();
                if (info.isValid()) {
                    children.get(i).getKey().makePure(builder);
                    value = children.get(i).getValue().equals("PLUS") ? value + info.getValue() :
                            value - info.getValue();
                }
            }
            boolean minus = childrenListToIntermediate((ArrayList<Pair<MulExpNode, String>>)children
                    .stream().filter(i -> !i.getKey().getExpressionInfo().isValid())
                    .collect(Collectors.toList()), builder);
            if (value != 0 || minus) {
                builder.putIntermediate(minus ?
                        AbstractElement.getSubElement(dstAbstractVariable
                                , builder.putIntConst(value), dstAbstractVariable) :
                        AbstractElement.getAddElement(dstAbstractVariable
                                , dstAbstractVariable, builder.putIntConst(value)), layer);
            }
        }
        else {
            childrenListToIntermediate(children, builder);
        }
    }

    @Override
    public void display(StringBuilder builder) {
        children.get(0).getKey().display(builder);
        for (int i = 1; i < children.size(); i++) {
            builder.append(children.get(i).getValue().equals("PLUS") ? " + " : " - ");
            children.get(i).getKey().display(builder);
        }
    }
}