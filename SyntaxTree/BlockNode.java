package SyntaxTree;

import java.util.HashMap;
import java.util.LinkedList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;

public class BlockNode extends TreeNode implements SymbolTable {
    private final LinkedList<TreeNode> children;
    private final HashMap<String, FParamNode> paramMap;
    private final HashMap<String, VarDefNode> varDefMap;
    private final HashMap<String, ConstDefNode> constDefMap;
    private final HashMap<String, HashMap<String, ? extends TreeNode>> mainMap;

    public BlockNode(Word mainWord, TreeNode father) {
        super(mainWord, father);
        mainMap = new HashMap<>();
        varDefMap = new HashMap<>();
        paramMap = new HashMap<>();
        constDefMap = new HashMap<>();
        children = new LinkedList<>();
        if (father instanceof FuncDefNode) {
            ((FuncDefNode)father).getParams().forEach(i -> {
                if (mainMap.containsKey(i.mainWord.getSourceWord())) {
                    handleError(this.getClass(), 0, i.mainWord);
                }
                else {
                    mainMap.put(i.mainWord.getSourceWord(), paramMap);
                    paramMap.put(i.mainWord.getSourceWord(), i);
                }
            });
        }
    }

    @Override
    public void addConstDef(TreeNode treeNode) {
        String name = treeNode.mainWord.getSourceWord();
        if (mainMap.containsKey(name)) {
            handleError(this.getClass(), 1);
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
            handleError(this.getClass(), 2);
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

    public void addStmt(TreeNode treeNode) {
        if (treeNode != null && (!(treeNode instanceof BlockNode)
                || !((BlockNode)treeNode).children.isEmpty())) {
            children.add(treeNode);
            selfConnectionCheck(treeNode);
        }
    }

    public void selfCheck(Word endWord) {
        if (father instanceof FuncDefNode) {
            if (((FuncDefNode)father).getType() == FuncDefNode.INT_TYPE
                    && (children.isEmpty() || !(children.getLast() instanceof ReturnNode))) {
                handleError(this.getClass(), 3, endWord);
            }
            else if (((FuncDefNode)father).getType() == FuncDefNode.VOID_TYPE
                    && (children.isEmpty() || !(children.getLast() instanceof ReturnNode))) {
                children.add(new ReturnNode(null, this));
            }
        }
    }

    @Override
    public void calculate() {
        children.forEach(TreeNode::calculate);
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        children.forEach(i -> i.toIntermediate(builder));
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append('{').append('\n');
        layer++;
        children.forEach(i -> {
            indentLayer(builder);
            i.display(builder);
            if (i instanceof AddExpNode) {
                builder.append(';').append('\n');
            }
        });
        layer--;
        indentLayer(builder);
        builder.append('}').append('\n');
    }
}