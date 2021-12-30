package SyntaxTree;

import Global.Settings;
import WordAnalyse.Word.Word;
import IntermediateCode.Operands.TagString;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.InterElement.AbstractElement;

public class LoopNode extends TreeNode {
    private TreeNode stmt;
    private TagString stopTag;
    private TagString skipTag;
    private LOrExpNode condition;

    public LoopNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        stmt = null;
        condition = null;
    }

    public void addCondition(TreeNode treeNode) {
        this.condition = (LOrExpNode)treeNode;
        selfConnectionCheck(treeNode);
    }

    public void addStmt(TreeNode treeNode) {
        if (treeNode != null) {
            this.stmt = treeNode;
            selfConnectionCheck(treeNode);
        }
    }

    protected TagString getStopTag() {
        return stopTag;
    }

    protected TagString getSkipTag() {
        return skipTag;
    }

    @Override
    public void calculate() {
        if (stmt != null) {
            stmt.calculate();
        }
        condition.calculate();
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.syntaxTreeExpressionOptimize && condition.getExpressionInfo().isValid()
                && condition.getExpressionInfo().isPure()
                && condition.getExpressionInfo().getValue() == 0) {
            condition.makePure(builder);
        }
        else {
            stopTag = builder.putTag("stop_tag");
            skipTag = builder.putTag("skip_tag");
            TagString loopTag = builder.putTag("loop_tag");
            condition.setTags(loopTag, stopTag);
            condition.toIntermediate(builder);
            layer++;
            builder.putIntermediate(AbstractElement.getTagElement(loopTag), layer);
            if (stmt != null) {
                stmt.toIntermediate(builder);
            }
            builder.putIntermediate(AbstractElement.getTagElement(skipTag), layer);
            condition.toIntermediate(builder);
            builder.putIntermediate(AbstractElement.getTagElement(stopTag), layer);
            layer--;
        }
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append("while (");
        condition.display(builder);
        builder.append(')').append(' ');
        if (stmt != null) {
            stmt.display(builder);
            if (stmt instanceof AddExpNode) {
                builder.append(';').append('\n');
            }
        }
        else {
            builder.append(';').append('\n');
        }
    }
}