package SyntaxTree;

import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class AssignNode extends TreeNode {
    private LValNode leftVal;
    private AddExpNode rightExp;

    public AssignNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        this.leftVal = null;
        this.rightExp = null;
    }

    public void setLVal(TreeNode treeNode) {
        this.leftVal = (LValNode)treeNode;
        selfConnectionCheck(treeNode);
        if (((LValNode)treeNode).getAbstractVarDef() instanceof ConstDefNode) {
            handleError(this.getClass(), 0, treeNode.mainWord);
        }
    }

    public void setRightExp(TreeNode treeNode) {
        this.rightExp = (AddExpNode)treeNode;
        selfConnectionCheck(treeNode);
    }

    @Override
    public void calculate() {
        leftVal.calculate();
        if (rightExp != null) {
            rightExp.calculate();
        }
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (rightExp == null) {
            if (leftVal.getAbstractVarDef().getDimensions().isEmpty()) {
                builder.putIntermediate(AbstractElement.getScanElement(leftVal
                        .getAbstractVarDef().getDstAbstractVariable()), layer);
            }
            else {
                AbstractVariable readAbstractVariable = builder.putVariable();
                builder.putIntermediate(AbstractElement.getScanElement(readAbstractVariable), layer);
                leftVal.updateValue(readAbstractVariable, builder);
            }
        }
        else {
            rightExp.toIntermediate(builder);
            leftVal.updateValue(rightExp.getDstAbstractVariable(), builder);
        }
    }

    @Override
    public void display(StringBuilder builder) {
        leftVal.display(builder);
        builder.append(' ').append('=').append(' ');
        if (rightExp != null) {
            rightExp.display(builder);
        }
        else {
            builder.append("getint()");
        }
        builder.append(';').append('\n');
    }
}