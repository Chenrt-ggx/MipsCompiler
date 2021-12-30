package SyntaxTree;

import Global.Pair;
import Global.Settings;
import java.util.ArrayList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class MulExpNode extends TreeNode {
    private ExpressionInfo expressionInfo;
    private AbstractVariable dstAbstractVariable;
    private final ArrayList<Pair<UnaryExpNode, String>> children;

    public MulExpNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        children = new ArrayList<>();
    }

    public void addUnary(String sign, TreeNode treeNode) {
        if (!children.isEmpty() && sign == null) {
            handleError(this.getClass(), 0);
        }
        children.add(new Pair<>((UnaryExpNode)treeNode, sign));
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
            boolean multZero = false;
            boolean isPure = children.get(0).getKey().getExpressionInfo().isPure();
            boolean isValid = children.get(0).getKey().getExpressionInfo().isValid();
            int value = children.get(0).getKey().getExpressionInfo().getValue();
            for (int i = 1; i < children.size(); i++) {
                ExpressionInfo ans = children.get(i).getKey().getExpressionInfo();
                isPure &= ans.isPure();
                isValid &= ans.isValid();
                if (ans.getValue() == 0) {
                    if (children.get(i).getValue().equals("MULT")) {
                        multZero = ans.isValid();
                        value = 0;
                    }
                }
                else {
                    value = children.get(i).getValue().equals("MULT") ? value * ans.getValue() :
                            children.get(i).getValue().equals("DIV") ? value / ans.getValue() :
                            value % ans.getValue();
                }
            }
            expressionInfo = new ExpressionInfo(value, isPure, isValid || multZero);
        }
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.syntaxTreeExpressionOptimize && expressionInfo.isValid()) {
            makePure(builder);
            dstAbstractVariable = builder.putIntConst(expressionInfo.getValue());
        }
        else {
            children.get(0).getKey().toIntermediate(builder);
            dstAbstractVariable = children.get(0).getKey().getDstAbstractVariable();
            for (int i = 1; i < children.size(); i++) {
                children.get(i).getKey().toIntermediate(builder);
                AbstractVariable nextDst = builder.putVariable();
                builder.putIntermediate(children.get(i).getValue().equals("MULT") ?
                        AbstractElement.getMulElement(nextDst, dstAbstractVariable,
                                children.get(i).getKey().getDstAbstractVariable()) :
                        children.get(i).getValue().equals("DIV") ?
                        AbstractElement.getDivElement(nextDst, dstAbstractVariable,
                                children.get(i).getKey().getDstAbstractVariable()) :
                        AbstractElement.getModElement(nextDst, dstAbstractVariable,
                                children.get(i).getKey().getDstAbstractVariable()), layer);
                dstAbstractVariable = nextDst;
            }
        }
    }

    @Override
    public void display(StringBuilder builder) {
        children.get(0).getKey().display(builder);
        for (int i = 1; i < children.size(); i++) {
            builder.append(
                    children.get(i).getValue().equals("MULT") ? " * " :
                    children.get(i).getValue().equals("DIV") ? " / " :
                    " % ");
            children.get(i).getKey().display(builder);
        }
    }
}