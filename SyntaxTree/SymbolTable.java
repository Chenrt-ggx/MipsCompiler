package SyntaxTree;

public interface SymbolTable {
    void addConstDef(TreeNode treeNode);

    void addVarDef(TreeNode treeNode);

    TreeNode queryDefine(String name);
}