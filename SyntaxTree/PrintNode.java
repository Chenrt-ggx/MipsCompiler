package SyntaxTree;

import java.util.ArrayList;
import WordAnalyse.Word.Word;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.InterElement.AbstractElement;

public class PrintNode extends TreeNode {
    private final Word printWord;
    private final String[] texts;
    private final ArrayList<AddExpNode> exps;

    public PrintNode(Word mainWord, TreeNode father, Word printWord) {
        super(mainWord, father);
        exps = new ArrayList<>();
        this.printWord = printWord;
        this.texts = mainWord.getSourceWord().split("%d");
        texts[0] = texts[0].substring(1);
        texts[texts.length - 1] = texts[texts.length - 1]
                .substring(0, texts[texts.length - 1].length() - 1);
        for (int i = 1; i < mainWord.getSourceWord().length() - 1; i++) {
            int val = mainWord.getSourceWord().charAt(i);
            if (val == 32 || val == 33 || val == 37 || (40 <= val && val <= 126)) {
                if (val == 37 && mainWord.getSourceWord().charAt(i + 1) != 'd') {
                    handleError(this.getClass(), 0);
                    break;
                }
                if (val == 92 && mainWord.getSourceWord().charAt(i + 1) != 'n') {
                    handleError(this.getClass(), 1);
                    break;
                }
            }
            else {
                handleError(this.getClass(), 2);
                break;
            }
        }
    }

    public void addExp(TreeNode treeNode) {
        exps.add((AddExpNode)treeNode);
        selfConnectionCheck(treeNode);
    }

    public void selfCheck() {
        if (texts.length - 1 != exps.size()) {
            handleError(this.getClass(), 3, printWord);
        }
    }

    @Override
    public void calculate() {
        exps.forEach(TreeNode::calculate);
    }

    @Override
    public void toIntermediate(IntermediateBuilder builder) {
        for (int i = exps.size() - 1; i >= 0; i--) {
            exps.get(i).toIntermediate(builder);
        }
        if (!texts[0].isEmpty()) {
            int addr = builder.putStringConst(texts[0]);
            builder.putIntermediate(AbstractElement.getPutStrElement(builder.putIntConst(addr)), layer);
        }
        for (int i = 0; i < exps.size(); i++) {
            builder.putIntermediate(AbstractElement.getPutNumElement(exps.get(i).getDstAbstractVariable()), layer);
            if (!texts[i + 1].isEmpty()) {
                int addr = builder.putStringConst(texts[i + 1]);
                builder.putIntermediate(AbstractElement.getPutStrElement(builder.putIntConst(addr)), layer);
            }
        }
    }

    @Override
    public void display(StringBuilder builder) {
        builder.append("printf(").append(mainWord.getSourceWord());
        exps.forEach(i -> {
            builder.append(',').append(' ');
            i.display(builder);
        });
        builder.append(')').append(';').append('\n');
    }
}