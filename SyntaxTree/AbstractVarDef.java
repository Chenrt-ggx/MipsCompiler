package SyntaxTree;

import java.util.ArrayList;
import IntermediateCode.Operands.AbstractVariable;

public interface AbstractVarDef {
    void addDimension(TreeNode treeNode);

    ArrayList<Integer> getDimensions();

    AbstractVariable getDstAbstractVariable();

    int offsetOf(int index);
}