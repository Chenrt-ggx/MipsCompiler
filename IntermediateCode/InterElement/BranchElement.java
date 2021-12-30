package IntermediateCode.InterElement;

import Global.Settings;
import java.util.HashSet;
import IntermediateCode.BasicBlock;
import MipsObjectCode.MipsAssembly;
import java.util.function.BiFunction;
import IntermediateCode.VirtualRunner;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.AbstractOperand;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public class BranchElement extends AbstractElement {
    @FunctionalInterface
    protected interface MapSingleToMipsAssembly {
        void accept(AbstractVariable src, TagString tag1, TagString tag2, MipsAssembly assembly);
    }

    @FunctionalInterface
    protected interface MapDoubleToMipsAssembly {
        void accept(AbstractVariable src1, AbstractVariable src2,
                    TagString tag1, TagString tag2, MipsAssembly assembly);
    }

    private final TagString tag1;
    private final TagString tag2;
    private AbstractVariable src1;
    private AbstractVariable src2;
    private final BiFunction<Integer,Integer,Boolean> execAction;
    private final MapSingleToMipsAssembly mapSingleToMipsAssembly;
    private final MapDoubleToMipsAssembly mapDoubleToMipsAssembly;

    protected BranchElement(TagString tag1, TagString tag2, AbstractVariable src
            , String name, BiFunction<Integer,Integer,Boolean> execAction
            , MapSingleToMipsAssembly mapSingleToMipsAssembly) {
        super(name);
        this.src1 = src;
        this.src2 = null;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.execAction = execAction;
        this.mapDoubleToMipsAssembly = null;
        this.mapSingleToMipsAssembly = mapSingleToMipsAssembly;
    }

    protected BranchElement(TagString tag1, TagString tag2, AbstractVariable src1, AbstractVariable src2
            , String name, BiFunction<Integer,Integer,Boolean> execAction
            , MapDoubleToMipsAssembly mapDoubleToMipsAssembly) {
        super(name);
        this.src1 = src1;
        this.src2 = src2;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.execAction = execAction;
        this.mapSingleToMipsAssembly = null;
        this.mapDoubleToMipsAssembly = mapDoubleToMipsAssembly;
    }

    public TagString[] getTags() {
        return new TagString[]{tag1, tag2};
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
        putLeftValueOnly(useSet, src1);
        if (src2 != null) {
            putLeftValueOnly(useSet, src2);
        }
    }

    @Override
    public void pushUpDef(HashSet<AbstractLeftValue> defSet) {
        /* Hey I like this so far! */
    }

    @Override
    public void countVariableUsage() {
        updateVariableWeight(src1);
        if (src2 != null) {
            updateVariableWeight(src2);
        }
    }

    @Override
    public void execute(VirtualRunner runner) {
        if (execAction.apply(runner.getVariable(src1), src2 == null ?
                null : runner.getVariable(src2))) {
            runner.processBranch(tag1);
        }
        else {
            runner.processBranch(tag2);
        }
    }

    @Override
    public AbstractOperand getMain() {
        return src1;
    }

    @Override
    public void setAddress(int address) {
        /* Hey I like this so far! */
    }

    @Override
    public int getSpace() {
        return 0;
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
    }

    @Override
    public void toMipsAssembly(MipsAssembly assembly) {
        if (Settings.optTempRegisterOptimize) {
            assembly.processFlushLocalRegister(basicBlock.getActiveOutSet());
        }
        if (src2 == null) {
            assert mapSingleToMipsAssembly != null;
            mapSingleToMipsAssembly.accept(src1, tag1, tag2, assembly);
        }
        else {
            assert mapDoubleToMipsAssembly != null;
            mapDoubleToMipsAssembly.accept(src1, src2, tag1, tag2, assembly);
        }
        if (Settings.optTempRegisterOptimize) {
            assembly.processClearLocalRegister();
        }
    }

    @Override
    public String displayExecute(VirtualRunner runner) {
        return name + " " + src1 + "(" + runner.tryGetVariable(src1) + ")" + (src2 != null
                ? ", " + src2 + "(" + runner.tryGetVariable(src2) + ")" : "")
                + " ? " + tag1 + " : " + tag2;
    }

    @Override
    public String toString() {
        return name + " " + src1 + (src2 != null ? ", " + src2 : "") + " ? " + tag1 + " : " + tag2;
    }
}