package IntermediateCode.InterElement;

import Global.Settings;
import java.util.HashSet;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.BasicBlock;
import IntermediateCode.VirtualRunner;
import java.util.function.ToIntBiFunction;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.AbstractOperand;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public class CalculateElement extends AbstractElement {
    @FunctionalInterface
    protected interface MapToMipsAssembly {
        void accept(AbstractLeftValue dst, AbstractVariable src1
                , AbstractVariable src2, MipsAssembly assembly);
    }

    private AbstractVariable src1;
    private AbstractVariable src2;
    private final AbstractLeftValue dst;
    private final MapToMipsAssembly mapToMipsAssembly;
    private final ToIntBiFunction<Integer, Integer> execAction;

    protected CalculateElement(AbstractVariable dst, AbstractVariable src1, AbstractVariable src2
            , String name, ToIntBiFunction<Integer, Integer> execAction
            , MapToMipsAssembly mapToMipsAssembly) {
        super(name);
        this.src1 = src1;
        this.src2 = src2;
        this.execAction = execAction;
        this.mapToMipsAssembly = mapToMipsAssembly;
        if (dst instanceof AbstractLeftValue) {
            this.dst = (AbstractLeftValue)dst;
        }
        else {
            System.err.println("unexpected write to uneditable variable");
            this.dst = null;
        }
    }

    public AbstractVariable[] getOperands() {
        return new AbstractVariable[]{src1, src2};
    }

    public AbstractVariable isSpreadAble() {
        if (Settings.constVariableSpread && src1 instanceof ConstValue && src2 instanceof ConstValue) {
            return new ConstValue(execAction.applyAsInt(
                    ((ConstValue)src1).getValue(), ((ConstValue)src2).getValue()));
        }
        else if (Settings.constVariableSpread && (name.equals("mul") || name.equals("div") || name.equals("mod"))
                && src1 instanceof ConstValue && ((ConstValue)src1).getValue() == 0) {
            return new ConstValue(0);
        }
        else if (Settings.constVariableSpread && (name.equals("mul"))
                && src2 instanceof ConstValue && ((ConstValue)src2).getValue() == 0) {
            return new ConstValue(0);
        }
        else if (Settings.constVariableSpread && (name.equals("mod"))
                && src2 instanceof ConstValue && ((ConstValue)src2).getValue() == 1) {
            return new ConstValue(0);
        }
        else if (Settings.rewriteVariableSpread && name.equals("add")
                && src1 instanceof ConstValue && ((ConstValue)src1).getValue() == 0
                && dst != src2 && src2 instanceof AbstractLeftValue) {
            return src2;
        }
        else if (Settings.rewriteVariableSpread && (name.equals("add") || name.equals("sub"))
                && src2 instanceof ConstValue && ((ConstValue)src2).getValue() == 0
                && dst != src1 && src1 instanceof AbstractLeftValue) {
            return src1;
        }
        else if (Settings.rewriteVariableSpread && name.equals("mul")
                && src1 instanceof ConstValue && ((ConstValue)src1).getValue() == 1
                && dst != src2 && src2 instanceof AbstractLeftValue) {
            return src2;
        }
        else if (Settings.rewriteVariableSpread && (name.equals("mul") || name.equals("div"))
                && src2 instanceof ConstValue && ((ConstValue)src2).getValue() == 1
                && dst != src1 && src1 instanceof AbstractLeftValue) {
            return src1;
        }
        return null;
    }

    @Override
    public void replaceVariable(AbstractLeftValue src, AbstractVariable replace) {
        if (src1 == src) {
            src1 = replace;
        }
        if (src2 == src) {
            src2 = replace;
        }
    }

    @Override
    public void pushUpUse(HashSet<AbstractLeftValue> useSet) {
        putLeftValueOnly(useSet, src1, src2);
    }

    @Override
    public void pushUpDef(HashSet<AbstractLeftValue> defSet) {
        if (!dst.equals(src1) && !dst.equals(src2)) {
            defSet.add(dst);
        }
    }

    @Override
    public void countVariableUsage() {
        updateVariableWeight(dst, src1, src2);
    }

    @Override
    public void execute(VirtualRunner runner) {
        runner.setVariable(dst, execAction.applyAsInt(runner
                .getVariable(src1), runner.getVariable(src2)));
    }

    @Override
    public AbstractOperand getMain() {
        return dst;
    }

    @Override
    public void setAddress(int address) {
        dst.setAddrOffset(address);
    }

    @Override
    public int getSpace() {
        return dst.getSpace();
    }

    @Override
    public void setPlaceInfo(BasicBlock basicBlock, int index) {
        super.setPlaceInfo(basicBlock, index);
        if (src1 instanceof AbstractLeftValue) {
            ((AbstractLeftValue)src1).setPlaceInfo(basicBlock, index);
        }
        if (src2 instanceof AbstractLeftValue) {
            ((AbstractLeftValue)src2).setPlaceInfo(basicBlock, index);
        }
        dst.setPlaceInfo(basicBlock, index);
    }

    @Override
    public void toMipsAssembly(MipsAssembly assembly) {
        mapToMipsAssembly.accept(dst, src1, src2, assembly);
        dst.updateVariable(assembly);
    }

    @Override
    public String displayExecute(VirtualRunner runner) {
        return name + " " + dst + ", " + src1 + "(" + runner.tryGetVariable(src1)
                + "), " + src2 + "(" + runner.tryGetVariable(src2) + ")";
    }

    @Override
    public String toString() {
        return name + " " + dst + ", " + src1 + ", " + src2;
    }
}