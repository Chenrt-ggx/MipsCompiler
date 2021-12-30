package SyntaxTree;

import Global.Pair;
import Global.Settings;
import java.util.ArrayList;
import java.util.LinkedList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class LValNode extends TreeNode {
    private ExpressionInfo expressionInfo;
    private final AbstractVarDef abstractVarDef;
    private AbstractVariable dstAbstractVariable;
    private final ArrayList<AddExpNode> dimensions;

    public LValNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        dimensions = new ArrayList<>();
        for (SymbolTable i = this.lastSymbolTable; i != null; i = ((TreeNode)i).lastSymbolTable) {
            TreeNode def = i.queryDefine(mainWord.getSourceWord());
            if (def != null) {
                if (def instanceof AbstractVarDef) {
                    abstractVarDef = (AbstractVarDef)def;
                }
                else {
                    abstractVarDef = null;
                    handleError(this.getClass(), 0);
                }
                return;
            }
        }
        abstractVarDef = null;
        handleError(this.getClass(), 1);
    }

    public void addDimension(TreeNode treeNode) {
        dimensions.add((AddExpNode)treeNode);
        selfConnectionCheck(treeNode);
    }

    protected ExpressionInfo getExpressionInfo() {
        calculate();
        return expressionInfo;
    }

    protected ArrayList<Integer> getDimensions() {
        if (abstractVarDef == null) {
            return null;
        }
        ArrayList<Integer> out = abstractVarDef.getDimensions();
        if (dimensions.size() > out.size()) {
            return null;
        }
        ArrayList<Integer> ret = new ArrayList<>(out.size() - dimensions.size());
        for (int i = dimensions.size(); i < out.size(); i++) {
            ret.add(out.get(i));
        }
        return ret;
    }

    protected AbstractVarDef getAbstractVarDef() {
        return abstractVarDef;
    }

    protected AbstractVariable getDstAbstractVariable() {
        return dstAbstractVariable;
    }

    private AbstractVariable getDimensionListAddr(ArrayList<Pair<Integer, AddExpNode>> dimensionsList
            , IntermediateBuilder builder) {
        dimensionsList.forEach(i -> i.getValue().toIntermediate(builder));
        AbstractVariable address = dimensionsList.get(0).getValue().getDstAbstractVariable();
        builder.putIntermediate(AbstractElement.getMulElement(address,
                address, builder.putIntConst(abstractVarDef
                        .offsetOf(dimensionsList.get(0).getKey()))), layer);
        for (int i = 1; i < dimensionsList.size(); i++) {
            AbstractVariable ans = dimensionsList.get(i).getValue().getDstAbstractVariable();
            AbstractVariable mul = builder.putIntConst(abstractVarDef
                    .offsetOf(dimensionsList.get(i).getKey()));
            builder.putIntermediate(AbstractElement.getMulElement(ans, ans, mul), layer);
            builder.putIntermediate(AbstractElement.getAddElement(address, address, ans), layer);
        }
        builder.putIntermediate(AbstractElement.getSllElement(
                address, address, builder.putIntConst(2)), layer);
        builder.putIntermediate(AbstractElement.getAddElement(
                address, address, abstractVarDef.getDstAbstractVariable()), layer);
        return address;
    }

    private AbstractVariable getAddr(IntermediateBuilder builder) {
        if (dimensions.isEmpty()) {
            return abstractVarDef.getDstAbstractVariable();
        }
        if (Settings.syntaxTreeExpressionOptimize) {
            int preCalculate = 0;
            boolean allMatch = true;
            for (int i = 0; i < dimensions.size(); i++) {
                ExpressionInfo dimension = dimensions.get(i).getExpressionInfo();
                if (dimension.isValid()) {
                    dimensions.get(i).makePure(builder);
                    preCalculate += dimension.getValue() * abstractVarDef.offsetOf(i) << 2;
                }
                allMatch &= dimension.isValid();
            }
            if (allMatch) {
                AbstractVariable address = builder.putVariable();
                builder.putIntermediate(AbstractElement.getAddElement(address,
                        abstractVarDef.getDstAbstractVariable(),
                        builder.putIntConst(preCalculate)), layer);
                return address;
            }
            ArrayList<Pair<Integer, AddExpNode>> dimensionsList = new ArrayList<>();
            for (int i = 0; i < dimensions.size(); i++) {
                if (!dimensions.get(i).getExpressionInfo().isValid()) {
                    dimensionsList.add(new Pair<>(i, dimensions.get(i)));
                }
            }
            AbstractVariable abstractVariable =  getDimensionListAddr(dimensionsList, builder);
            builder.putIntermediate(AbstractElement.getAddElement(abstractVariable, abstractVariable,
                    builder.putIntConst(preCalculate)), layer);
            return abstractVariable;
        }
        else {
            ArrayList<Pair<Integer, AddExpNode>> dimensionsList = new ArrayList<>();
            for (int i = 0; i < dimensions.size(); i++) {
                dimensionsList.add(new Pair<>(i, dimensions.get(i)));
            }
            return getDimensionListAddr(dimensionsList, builder);
        }
    }

    protected void updateValue(AbstractVariable srcAbstractVariable, IntermediateBuilder intermediate) {
        if (abstractVarDef.getDimensions().isEmpty()) {
            intermediate.putIntermediate(AbstractElement.getAddElement(abstractVarDef
                    .getDstAbstractVariable(), srcAbstractVariable
                    , intermediate.putIntConst(0)), layer);
        }
        else {
            intermediate.putIntermediate(AbstractElement
                    .getStoreElement(srcAbstractVariable, getAddr(intermediate)), layer);
        }
    }

    protected void makePure(IntermediateBuilder builder) {
        dimensions.stream().filter(i -> !i.getExpressionInfo()
                .isPure()).forEach(i -> i.makePure(builder));
    }

    @Override
    public void calculate() {
        if (expressionInfo == null) {
            boolean isPure = true;
            boolean isValid = true;
            LinkedList<Integer> path = new LinkedList<>();
            for (AddExpNode i : dimensions) {
                ExpressionInfo ans = i.getExpressionInfo();
                path.add(ans.getValue());
                isValid &= ans.isValid();
                isPure &= ans.isPure();
            }
            expressionInfo = abstractVarDef instanceof ConstDefNode ?
                    new ExpressionInfo(((ConstDefNode)abstractVarDef).getValue(path), isPure, isValid) :
                    abstractVarDef instanceof VarDefNode ?
                    new ExpressionInfo(((VarDefNode)abstractVarDef).getValue(path), isPure, false) :
                    new ExpressionInfo(0, isPure, false);
        }
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        if (Settings.syntaxTreeExpressionOptimize && expressionInfo.isValid()) {
            makePure(builder);
            dstAbstractVariable = builder.putIntConst(expressionInfo.getValue());
        }
        else {
            dstAbstractVariable = builder.putVariable();
            if (abstractVarDef.getDimensions().isEmpty()) {
                builder.putIntermediate(AbstractElement.getAddElement(
                        dstAbstractVariable, abstractVarDef.getDstAbstractVariable(),
                        builder.putIntConst(0)), layer);
            }
            else if (dimensions.size() == abstractVarDef.getDimensions().size()) {
                builder.putIntermediate(AbstractElement
                        .getLoadElement(dstAbstractVariable, getAddr(builder)), layer);
            }
            else {
                builder.putIntermediate(AbstractElement.getAddElement(
                        dstAbstractVariable, getAddr(builder), builder.putIntConst(0)), layer);
            }
        }
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append(mainWord.getSourceWord());
        dimensions.forEach(i -> {
            builder.append('[');
            i.display(builder);
            builder.append(']');
        });
    }
}