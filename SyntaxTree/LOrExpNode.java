package SyntaxTree;

import Global.Settings;
import java.util.ArrayList;
import WordAnalyse.Word.Word;
import java.util.stream.Collectors;
import IntermediateCode.Operands.TagString;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.InterElement.AbstractElement;

public class LOrExpNode extends TreeNode {
    private TagString trueTag;
    private TagString falseTag;
    private ExpressionInfo expressionInfo;
    private final ArrayList<LAndExpNode> children;

    public LOrExpNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        children = new ArrayList<>();
    }

    public void addLAnd(TreeNode treeNode) {
        children.add((LAndExpNode)treeNode);
        selfConnectionCheck(treeNode);
    }

    protected ExpressionInfo getExpressionInfo() {
        calculate();
        return expressionInfo;
    }

    protected void setTags(TagString trueTag, TagString falseTag) {
        this.trueTag = trueTag;
        this.falseTag = falseTag;
    }

    protected void makePure(IntermediateBuilder builder) {
        children.stream().filter(i -> !i.getExpressionInfo()
                .isPure()).forEach(i -> i.makePure(builder));
    }

    @Override
    public void calculate() {
        if (expressionInfo == null) {
            children.forEach(TreeNode::calculate);
            if (children.stream().allMatch(i -> i.getExpressionInfo().isPure())
                    && children.stream().anyMatch(i -> i.getExpressionInfo().isValid()
                    && i.getExpressionInfo().getValue() != 0)) {
                expressionInfo = new ExpressionInfo(1, true, true);
                return;
            }
            if (children.stream().allMatch(i -> i.getExpressionInfo().isPure()
                    && i.getExpressionInfo().isValid()
                    && i.getExpressionInfo().getValue() == 0)) {
                expressionInfo = new ExpressionInfo(0, true, true);
                return;
            }
            if (children.stream().anyMatch(i -> i.getExpressionInfo().isValid()
                    && i.getExpressionInfo().getValue() != 0)) {
                expressionInfo = new ExpressionInfo(1, false, true);
                return;
            }
            if (children.stream().allMatch(i -> i.getExpressionInfo().isValid()
                    && i.getExpressionInfo().getValue() == 0)) {
                expressionInfo = new ExpressionInfo(0, false, true);
                return;
            }
            expressionInfo = new ExpressionInfo(0, false, false);
        }
    }

    private void childrenListToIntermediate(ArrayList<LAndExpNode> childrenList
            , IntermediateBuilder builder) {
        for (int i = 0; i < childrenList.size(); i++) {
            TagString ignoreTag = builder.putTag("ignore_tag");
            childrenList.get(i).setTags(trueTag, falseTag, ignoreTag, i + 1 == childrenList.size());
            childrenList.get(i).toIntermediate(builder);
            builder.putIntermediate(AbstractElement.getTagElement(ignoreTag), layer);
        }
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.syntaxTreeExpressionOptimize) {
            if (expressionInfo.isPure() && expressionInfo.isValid() && expressionInfo.getValue() == 0) {
                builder.putIntermediate(AbstractElement.getJumpElement(falseTag), layer);
            }
            else if (expressionInfo.isPure() && expressionInfo.isValid()) {
                builder.putIntermediate(AbstractElement.getJumpElement(trueTag), layer);
            }
            else {
                childrenListToIntermediate((ArrayList<LAndExpNode>)children.stream()
                        .filter(i -> !i.getExpressionInfo().isPure()
                                || !i.getExpressionInfo().isValid()
                                || i.getExpressionInfo().getValue() != 0)
                        .collect(Collectors.toList()), builder);
            }
        }
        else {
            childrenListToIntermediate(children, builder);
        }
    }

    @Override
    public void display(StringBuilder builder) {
        children.get(0).display(builder);
        for (int i = 1; i < children.size(); i++) {
            builder.append(" || ");
            children.get(i).display(builder);
        }
    }
}