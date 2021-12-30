package SyntaxTree;

import Global.Pair;
import Global.Settings;
import java.util.HashSet;
import java.util.ArrayList;
import WordAnalyse.Word.Word;
import IntermediateCode.Operands.TagString;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class FuncCallNode extends TreeNode {
    private final FuncDefNode funcDef;
    private final ArrayList<AddExpNode> params;
    private AbstractVariable dstAbstractVariable;

    public FuncCallNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        params = new ArrayList<>();
        for (SymbolTable i = this.lastSymbolTable; i != null; i = ((TreeNode)i).lastSymbolTable) {
            TreeNode def = i.queryDefine(mainWord.getSourceWord());
            if (def != null) {
                if (def instanceof FuncDefNode) {
                    this.funcDef = (FuncDefNode)def;
                    for (TreeNode j = this.father; j != null; j = j.father) {
                        if (j instanceof FuncDefNode) {
                            ((FuncDefNode)j).addCaller(funcDef);
                            break;
                        }
                    }
                }
                else {
                    this.funcDef = null;
                    handleError(this.getClass(), 0);
                }
                return;
            }
        }
        this.funcDef = null;
        handleError(this.getClass(), 1);
    }

    public void addParams(TreeNode treeNode) {
        params.add((AddExpNode)treeNode);
        selfConnectionCheck(treeNode);
    }

    private boolean notSame(ArrayList<Integer> call, ArrayList<Integer> def) {
        if (def == null || call == null || call.size() != def.size()) {
            return true;
        }
        for (int i = 1; i < def.size(); i++) {
            if (!def.get(i).equals(call.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void selfCheck() {
        if (funcDef != null) {
            if (params.size() != funcDef.getParams().size()) {
                handleError(this.getClass(), 2);
            }
            for (int i = 0; i < funcDef.getParams().size() && i < params.size(); i++) {
                if (notSame(params.get(i).getDimensions(), funcDef.getParams().get(i).getDimensions())) {
                    handleError(this.getClass(), 3);
                    break;
                }
            }
        }
    }

    protected ArrayList<Integer> getDimensions() {
        return funcDef.getType() == FuncDefNode.INT_TYPE ? new ArrayList<>() : null;
    }

    protected AbstractVariable getDstAbstractVariable() {
        return dstAbstractVariable;
    }

    protected void makePure(IntermediateBuilder builder) {
        for (int i = params.size() - 1; i >= 0; i--) {
            params.get(i).toIntermediate(builder);
        }
        params.forEach(i -> builder.putIntermediate(AbstractElement
                .getParamElement(i.getDstAbstractVariable()), layer));
        builder.putIntermediate(AbstractElement.getCallElement(funcDef.getFunctionDef()), layer);
    }

    @Override
    public void calculate() {
        params.forEach(TreeNode::calculate);
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        HashSet<FuncDefNode> unique = new HashSet<>();
        funcDef.searchCallers(unique);
        if (Settings.autoInlineFunction && !unique.contains(funcDef) && funcDef.getSize() < 16) {
            if (funcDef.getType() == FuncDefNode.INT_TYPE) {
                dstAbstractVariable = builder.putVariable();
            }
            inlineStack.add(new Pair<>(builder.putTag("inline_tag"), dstAbstractVariable));
            for (int i = params.size() - 1; i >= 0; i--) {
                params.get(i).toIntermediate(builder);
            }
            funcDef.flushParams(builder);
            for (int i = 0; i < funcDef.getParams().size(); i++) {
                builder.putIntermediate(AbstractElement.getAddElement(
                        funcDef.getParams().get(i).getDstAbstractVariable(),
                        params.get(i).getDstAbstractVariable(),
                        builder.putIntConst(0)), layer);
            }
            funcDef.getBlocks().toIntermediate(builder);
            TagString endTag = inlineStack.removeLast().getKey();
            builder.putIntermediate(AbstractElement.getTagElement(endTag), layer);
        }
        else {
            makePure(builder);
            if (funcDef.getType() == FuncDefNode.INT_TYPE) {
                dstAbstractVariable = builder.putVariable();
                builder.putIntermediate(AbstractElement.getAddElement(dstAbstractVariable,
                        builder.putReturnValue(), builder.putIntConst(0)), layer);
            }
        }
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append(mainWord.getSourceWord());
        builder.append('(');
        if (!params.isEmpty()) {
            params.get(0).display(builder);
            for (int i = 1; i < params.size(); i++) {
                builder.append(',').append(' ');
                params.get(i).display(builder);
            }
        }
        builder.append(')');
    }
}