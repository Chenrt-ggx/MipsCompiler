package SyntaxTree;

import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.InterElement.AbstractElement;

public class ReturnNode extends TreeNode {
    private AddExpNode returnExp;
    private final FuncDefNode funcDef;

    public ReturnNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        this.returnExp = null;
        for (TreeNode i = this.father; i != null; i = i.father) {
            if (i instanceof FuncDefNode) {
                this.funcDef = (FuncDefNode)i;
                return;
            }
        }
        this.funcDef = null;
        handleError(this.getClass(), 0);
    }

    public void setExp(TreeNode treeNode) {
        this.returnExp = (AddExpNode)treeNode;
        selfConnectionCheck(treeNode);
    }

    public void selfCheck() {
        if (returnExp != null && funcDef.getType() == FuncDefNode.VOID_TYPE) {
            handleError(this.getClass(), 1);
        }
        if (returnExp == null && funcDef.getType() == FuncDefNode.INT_TYPE) {
            handleError(this.getClass(), 2);
        }
    }

    @Override
    public void calculate() {
        if (returnExp != null) {
            returnExp.calculate();
        }
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (inlineStack.isEmpty()) {
            if (returnExp == null || funcDef.isMain()) {
                if (returnExp != null) {
                    returnExp.makePure(builder);
                }
                builder.putIntermediate(AbstractElement.getExitElement(null), layer);
            }
            else {
                returnExp.toIntermediate(builder);
                builder.putIntermediate(AbstractElement.getExitElement(returnExp.getDstAbstractVariable()), layer);
            }
        }
        else {
            if (returnExp != null) {
                returnExp.toIntermediate(builder);
                builder.putIntermediate(AbstractElement.getAddElement(inlineStack.getLast().getValue()
                        , returnExp.getDstAbstractVariable(), builder.putIntConst(0)), layer);
            }
            builder.putIntermediate(AbstractElement.getJumpElement(inlineStack.getLast().getKey()), layer);
        }
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append("return");
        if (returnExp != null) {
            builder.append(' ');
            returnExp.display(builder);
        }
        builder.append(';').append('\n');
    }
}