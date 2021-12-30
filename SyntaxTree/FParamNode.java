package SyntaxTree;

import java.util.ArrayList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;

public class FParamNode extends TreeNode implements AbstractVarDef {
    private int place;
    private final ArrayList<Integer> dimensions;
    private AbstractVariable dstAbstractVariable;

    public FParamNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        dimensions = new ArrayList<>();
    }

    @Override
    public void addDimension(TreeNode treeNode) {
        if (treeNode != null) {
            ExpressionInfo val = ((AddExpNode)treeNode).getExpressionInfo();
            if (val.isValid() && val.getValue() > 0) {
                dimensions.add(val.getValue());
            }
            else {
                handleError(this.getClass(), 0);
            }
        }
        else {
            dimensions.add(-1);
        }
    }

    protected void flush(IntermediateBuilder builder) {
        dstAbstractVariable = builder.putVariable();
    }

    @Override
    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    @Override
    public AbstractVariable getDstAbstractVariable() {
        return dstAbstractVariable;
    }

    @Override
    public int offsetOf(int index) {
        int ans = 1;
        for (int i = index + 1; i < dimensions.size(); i++) {
            ans *= dimensions.get(i);
        }
        return ans;
    }

    protected void setPlace(int place) {
        this.place = place;
    }

    @Override
    public void calculate() {
        /* Hey I like this so far! */
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        dstAbstractVariable = builder.putParamDef(place);
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append("int ");
        builder.append(mainWord.getSourceWord());
        dimensions.forEach(i -> {
            builder.append('[');
            if (i != -1) {
                builder.append(i);
            }
            builder.append(']');
        });
    }
}