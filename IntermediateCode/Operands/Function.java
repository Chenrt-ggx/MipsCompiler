package IntermediateCode.Operands;

import Global.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import IntermediateCode.BasicBlock;

public class Function extends AbstractOperand {
    private int space;
    private final boolean isMain;
    private final ArrayList<ParamVariable> params;
    private final HashMap<Pair<BasicBlock, Integer>, HashSet<AbstractLeftValue>> actives;

    public Function(String operand, ArrayList<ParamVariable> params, boolean isMain) {
        super(operand);
        this.space = -1;
        this.params = params;
        this.isMain = isMain;
        this.actives = new HashMap<>();
    }

    public boolean isMain() {
        return isMain;
    }

    @Override
    public int getSpace() {
        return space;
    }

    public int getParams() {
        return params.size();
    }

    public ParamVariable getParam(int index) {
        return params.get(index);
    }

    public HashSet<AbstractLeftValue> getActiveVariables(BasicBlock basicBlock, int index) {
        return actives.get(new Pair<>(basicBlock, index));
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public void updateActives(BasicBlock basicBlock, int index, HashSet<AbstractLeftValue> actives) {
        this.actives.put(new Pair<>(basicBlock, index), new HashSet<>(actives));
    }
}