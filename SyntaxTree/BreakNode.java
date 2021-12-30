package SyntaxTree;

import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.InterElement.AbstractElement;

public class BreakNode extends TreeNode {
    private final LoopNode lastLoop;

    public BreakNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        TreeNode lastNode = father;
        while (lastNode instanceof BlockNode || lastNode instanceof ConditionNode) {
            lastNode = lastNode.father;
        }
        if (lastNode instanceof LoopNode) {
            lastLoop = (LoopNode)lastNode;
        }
        else {
            handleError(this.getClass(), 0);
            lastLoop = null;
        }
    }

    @Override
    public void calculate() {
        /* Hey I like this so far! */
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        builder.putIntermediate(AbstractElement.getJumpElement(lastLoop.getStopTag()), layer);
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append("break").append(';').append('\n');
    }
}