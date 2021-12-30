package SyntaxTree;

import Global.Settings;
import java.util.ArrayList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class UnaryExpNode extends TreeNode {
    private String type;
    private TreeNode child;
    private ExpressionInfo expressionInfo;
    private AbstractVariable dstAbstractVariable;

    public UnaryExpNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        this.type = null;
        this.child = null;
    }

    public void setUnary(String type, TreeNode treeNode) {
        this.type = type;
        this.child = treeNode;
        selfConnectionCheck(treeNode);
    }

    protected ExpressionInfo getExpressionInfo() {
        calculate();
        return expressionInfo;
    }

    protected ArrayList<Integer> getDimensions() {
        if (child instanceof AddExpNode) {
            return ((AddExpNode)child).getDimensions();
        }
        else if (child instanceof UnaryExpNode) {
            return ((UnaryExpNode)child).getDimensions();
        }
        else if (child instanceof LValNode) {
            return ((LValNode)child).getDimensions();
        }
        else if (child instanceof FuncCallNode) {
            return ((FuncCallNode)child).getDimensions();
        }
        else {
            return new ArrayList<>();
        }
    }

    protected AbstractVariable getDstAbstractVariable() {
        return dstAbstractVariable;
    }

    protected void makePure(IntermediateBuilder builder) {
        if (child instanceof AddExpNode && !((AddExpNode)child).getExpressionInfo().isPure()) {
            ((AddExpNode)child).makePure(builder);
        }
        else if (child instanceof UnaryExpNode && !((UnaryExpNode)child).getExpressionInfo().isPure()) {
            ((UnaryExpNode)child).makePure(builder);
        }
        else if (child instanceof LValNode && !((LValNode)child).getExpressionInfo().isPure()) {
            ((LValNode)child).makePure(builder);
        }
        else if (child instanceof FuncCallNode) {
            ((FuncCallNode)child).makePure(builder);
        }
    }

    @Override
    public void calculate() {
        if (expressionInfo == null) {
            child.calculate();
            if (child instanceof UnaryExpNode) {
                boolean isPure = ((UnaryExpNode)child).getExpressionInfo().isPure();
                boolean isValid = ((UnaryExpNode)child).getExpressionInfo().isValid();
                int value = ((UnaryExpNode)child).getExpressionInfo().getValue();
                value = type.equals("PLUS") ? value :
                        type.equals("MINU") ? -value :
                        value == 0 ? 1 : 0;
                expressionInfo = new ExpressionInfo(value, isPure, isValid);
            }
            else if (child instanceof ValueNode) {
                expressionInfo = ((ValueNode)child).getExpressionInfo();
            }
            else if (child instanceof AddExpNode) {
                expressionInfo = ((AddExpNode)child).getExpressionInfo();
            }
            else if (child instanceof LValNode) {
                expressionInfo = ((LValNode)child).getExpressionInfo();
            }
            else {
                expressionInfo = new ExpressionInfo(0, false, false);
            }
        }
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.syntaxTreeExpressionOptimize && expressionInfo.isValid()) {
            makePure(builder);
            dstAbstractVariable = builder.putIntConst(expressionInfo.getValue());
        }
        else {
            child.toIntermediate(builder);
            if (child instanceof ValueNode) {
                dstAbstractVariable = ((ValueNode)child).getDstAbstractVariable();
            }
            else if (child instanceof AddExpNode) {
                dstAbstractVariable = ((AddExpNode)child).getDstAbstractVariable();
            }
            else if (child instanceof LValNode) {
                dstAbstractVariable = ((LValNode)child).getDstAbstractVariable();
            }
            else if (child instanceof FuncCallNode) {
                dstAbstractVariable = ((FuncCallNode)child).getDstAbstractVariable();
            }
            else if (type.equals("MINU")) {
                dstAbstractVariable = ((UnaryExpNode)child).getDstAbstractVariable();
                builder.putIntermediate(AbstractElement.getSubElement(((UnaryExpNode)child)
                        .getDstAbstractVariable(), builder.putIntConst(0),
                        ((UnaryExpNode)child).getDstAbstractVariable()), layer);
            }
            else if (type.equals("NOT")) {
                dstAbstractVariable = ((UnaryExpNode)child).getDstAbstractVariable();
                builder.putIntermediate(AbstractElement.getSeqElement(((UnaryExpNode)child)
                        .getDstAbstractVariable(), builder.putIntConst(0),
                        ((UnaryExpNode)child).getDstAbstractVariable()), layer);
            }
            else {
                dstAbstractVariable = ((UnaryExpNode)child).getDstAbstractVariable();
            }
        }
    }

    @Override
    public void display(StringBuilder builder) {
        if (type != null) {
            builder.append(type.equals("PLUS") ? "+" :
                    type.equals("MINU") ? "-" :
                    "!");
        }
        if (child instanceof AddExpNode) {
            builder.append('(');
        }
        child.display(builder);
        if (child instanceof AddExpNode) {
            builder.append(')');
        }
    }
}