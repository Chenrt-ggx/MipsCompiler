package SyntaxTree;

import Global.Settings;
import WordAnalyse.Word.Word;
import IntermediateCode.Operands.TagString;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.InterElement.AbstractElement;

public class ConditionNode extends TreeNode {
    private TreeNode thenStmt;
    private TreeNode elseStmt;
    private LOrExpNode condition;

    public ConditionNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        thenStmt = null;
        elseStmt = null;
        condition = null;
    }

    public void addCondition(TreeNode treeNode) {
        this.condition = (LOrExpNode)treeNode;
        selfConnectionCheck(treeNode);
    }

    public void setThenStmt(TreeNode treeNode) {
        if (treeNode != null) {
            this.thenStmt = treeNode;
            selfConnectionCheck(treeNode);
        }
    }

    public void setElseStmt(TreeNode treeNode) {
        if (treeNode != null) {
            this.elseStmt = treeNode;
            selfConnectionCheck(treeNode);
        }
    }

    @Override
    public void calculate() {
        if (thenStmt != null) {
            thenStmt.calculate();
        }
        if (elseStmt != null) {
            elseStmt.calculate();
        }
        condition.calculate();
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        ExpressionInfo ans = condition.getExpressionInfo();
        boolean ignoreThen = ans.isValid() && ans.isPure() && ans.getValue() == 0;
        boolean ignoreElse = ans.isValid() && ans.isPure() && ans.getValue() != 0;
        if (Settings.syntaxTreeExpressionOptimize && (ignoreThen || ignoreElse)) {
            if (!ignoreThen && thenStmt != null) {
                thenStmt.toIntermediate(builder);
            }
            if (!ignoreElse && elseStmt != null) {
                elseStmt.toIntermediate(builder);
            }
        }
        else {
            TagString thenTag = builder.putTag("then_tag");
            TagString elseTag = builder.putTag("else_tag");
            condition.setTags(thenTag, elseTag);
            condition.toIntermediate(builder);
            builder.putIntermediate(AbstractElement.getTagElement(thenTag), layer);
            if (thenStmt != null) {
                thenStmt.toIntermediate(builder);
            }
            TagString endTag = builder.putTag("end_tag");
            builder.putIntermediate(AbstractElement.getJumpElement(endTag), layer);
            builder.putIntermediate(AbstractElement.getTagElement(elseTag), layer);
            if (elseStmt != null) {
                elseStmt.toIntermediate(builder);
            }
            builder.putIntermediate(AbstractElement.getTagElement(endTag), layer);
        }
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append("if (");
        condition.display(builder);
        builder.append(')').append(' ');
        if (thenStmt != null) {
            thenStmt.display(builder);
            if (thenStmt instanceof AddExpNode) {
                builder.append(';').append('\n');
            }
        }
        else {
            builder.append(';').append('\n');
        }
        if (elseStmt != null) {
            indentLayer(builder);
            builder.append("else ");
            elseStmt.display(builder);
            if (elseStmt instanceof AddExpNode) {
                builder.append(';').append('\n');
            }
        }
    }
}