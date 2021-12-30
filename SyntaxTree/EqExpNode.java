package SyntaxTree;

import Global.Pair;
import Global.Settings;
import java.util.ArrayList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class EqExpNode extends TreeNode {
    private ExpressionInfo expressionInfo;
    private AbstractVariable dstAbstractVariable;
    private final ArrayList<Pair<RelExpNode, String>> children;

    public EqExpNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        children = new ArrayList<>();
    }

    public void addRel(String sign, TreeNode treeNode) {
        if (!children.isEmpty() && sign == null) {
            handleError(this.getClass(), 0);
        }
        children.add(new Pair<>((RelExpNode)treeNode, sign));
        selfConnectionCheck(treeNode);
    }

    protected ExpressionInfo getExpressionInfo() {
        calculate();
        return expressionInfo;
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
                if (i >= 2 && ans.isValid() && children.get(i).getValue().equals("EQL")
                        && (ans.getValue() < 0 || ans.getValue() > 1)) {
                    value = 0;
                    isValid = true;
                }
                else if (i >= 2 && ans.isValid() && children.get(i).getValue().equals("NEQ")
                        && (ans.getValue() < 0 || ans.getValue() > 1)) {
                    value = 1;
                    isValid = true;
                }
                else {
                    value = children.get(i).getValue().equals("EQL") ? value == ans.getValue() ? 1 : 0 :
                            value != ans.getValue() ? 1 : 0;
                }
            }
            expressionInfo = new ExpressionInfo(value, isPure, isValid);
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
                builder.putIntermediate(children.get(i).getValue().equals("EQL") ?
                        AbstractElement.getSeqElement(nextDst, dstAbstractVariable,
                                children.get(i).getKey().getDstAbstractVariable()) :
                        AbstractElement.getSneElement(nextDst, dstAbstractVariable,
                                children.get(i).getKey().getDstAbstractVariable()), layer);
                dstAbstractVariable = nextDst;
            }
        }
    }

    @Override
    public void display(StringBuilder builder) {
        children.get(0).getKey().display(builder);
        for (int i = 1; i < children.size(); i++) {
            builder.append(children.get(0).getValue().equals("EQL") ? " == " : " != ");
            children.get(i).getKey().display(builder);
        }
    }
}