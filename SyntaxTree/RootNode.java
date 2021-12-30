package SyntaxTree;

import Global.Settings;
import java.util.HashMap;
import java.util.LinkedList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;

public class RootNode extends TreeNode implements SymbolTable {
    private final LinkedList<TreeNode> children;
    private final HashMap<String, VarDefNode> varDefMap;
    private final HashMap<String, FuncDefNode> funcDefMap;
    private final LinkedList<ConstDefNode> constDefInFunction;
    private final HashMap<String, ConstDefNode> constDefMap;
    private final HashMap<String, HashMap<String, ? extends TreeNode>> mainMap;

    public RootNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        mainMap = new HashMap<>();
        varDefMap = new HashMap<>();
        funcDefMap = new HashMap<>();
        constDefMap = new HashMap<>();
        children = new LinkedList<>();
        constDefInFunction = new LinkedList<>();
    }

    @Override
    public void addConstDef(TreeNode treeNode) {
        String name = treeNode.mainWord.getSourceWord();
        if (mainMap.containsKey(name)) {
            handleError(this.getClass(), 0);
        }
        else {
            children.add(treeNode);
            mainMap.put(name, constDefMap);
            constDefMap.put(name, (ConstDefNode)treeNode);
            selfConnectionCheck(treeNode);
        }
    }

    @Override
    public void addVarDef(TreeNode treeNode) {
        String name = treeNode.mainWord.getSourceWord();
        if (mainMap.containsKey(name)) {
            handleError(this.getClass(), 1);
        }
        else {
            children.add(treeNode);
            mainMap.put(name, varDefMap);
            varDefMap.put(name, (VarDefNode)treeNode);
            selfConnectionCheck(treeNode);
        }
    }

    @Override
    public TreeNode queryDefine(String name) {
        HashMap<String, ? extends TreeNode> map = mainMap.get(name);
        if (map == null) {
            return null;
        }
        return map.get(name);
    }

    public void addFuncDef(TreeNode treeNode) {
        String name = treeNode.mainWord.getSourceWord();
        if (mainMap.containsKey(name)) {
            handleError(this.getClass(), 2);
        }
        else {
            children.add(treeNode);
            mainMap.put(name, funcDefMap);
            funcDefMap.put(name, (FuncDefNode)treeNode);
            selfConnectionCheck(treeNode);
        }
    }

    protected void addConstDefInFunction(ConstDefNode constDefNode) {
        constDefInFunction.add(constDefNode);
    }

    public void optimize() {
        calculate();
    }

    @Override
    public void calculate() {
        children.forEach(TreeNode::calculate);
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        layer = 0;
        inlineStack = new LinkedList<>();
        children.stream().filter(i -> !(i instanceof FuncDefNode))
                .forEach(i -> i.toIntermediate(builder));
        if (Settings.moveConstInFunctionToGlobal) {
            constDefInFunction.forEach(i -> i.toIntermediate(builder));
            constDefInFunction.forEach(ConstDefNode::setDumpedInRootNode);
        }
        children.stream().filter(i -> i instanceof FuncDefNode)
                .forEach(i -> i.toIntermediate(builder));
    }

    @Override
    public void display(StringBuilder builder) {
        layer = 0;
        builder.append("#include <stdio.h>\n\n");
        builder.append("int getint() {\n").append("\tint ret = 0;\n");
        builder.append("\tscanf(\"%d\", &ret);\n").append("\treturn ret;\n}\n\n");
        children.forEach(i -> i.display(builder));
    }
}