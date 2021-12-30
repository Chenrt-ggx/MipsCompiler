package SyntaxTree;

import Global.Settings;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class ValueNode extends TreeNode {
    private int value;
    private ExpressionInfo expressionInfo;
    private AbstractVariable dstAbstractVariable;

    public ValueNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
    }

    public void setValue(int value) {
        this.value = value;
    }

    protected ExpressionInfo getExpressionInfo() {
        calculate();
        return expressionInfo;
    }

    protected AbstractVariable getDstAbstractVariable() {
        return dstAbstractVariable;
    }

    @Override
    public void calculate() {
        if (expressionInfo == null) {
            expressionInfo = new ExpressionInfo(value, true, true);
        }
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.syntaxTreeExpressionOptimize) {
            dstAbstractVariable = builder.putIntConst(value);
        }
        else {
            dstAbstractVariable = builder.putVariable();
            builder.putIntermediate(AbstractElement.getAddElement(dstAbstractVariable,
                    builder.putIntConst(value), builder.putIntConst(0)), layer);
        }
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append(value);
    }
}