package IntermediateCode.InterElement;

import java.util.Objects;
import java.util.HashSet;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.BasicBlock;
import IntermediateCode.VirtualRunner;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.AbstractOperand;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public abstract class AbstractElement {
    public static BranchElement getBnzElement(TagString tag1
            , TagString tag2, AbstractVariable src) {
        return new BranchElement(tag1, tag2, src, "bnz", (x, y) -> x != 0,
            (x, y, z, assembly) -> assembly.processBranchNotZero(x, y, z));
    }

    public static BranchElement getBneElement(TagString tag1
            , TagString tag2, AbstractVariable src1, AbstractVariable src2) {
        return new BranchElement(tag1, tag2, src1, src2, "bne", (x, y) -> !Objects.equals(x, y),
            (x, y, z, w, assembly) -> assembly.processBranchNotEqual(x, y, z, w));
    }

    public static BranchElement getBeqElement(TagString tag1
            , TagString tag2, AbstractVariable src1, AbstractVariable src2) {
        return new BranchElement(tag1, tag2, src1, src2, "beq", Objects::equals,
            (x, y, z, w, assembly) -> assembly.processBranchIfEqual(x, y, z, w));
    }

    public static BranchElement getBgezElement(TagString tag1
            , TagString tag2, AbstractVariable src) {
        return new BranchElement(tag1, tag2, src, "bgez", (x, y) -> x >= 0,
            (x, y, z, assembly) -> assembly.processBranchGreaterOrEqualZero(x, y, z));
    }

    public static BranchElement getBgtzElement(TagString tag1
            , TagString tag2, AbstractVariable src) {
        return new BranchElement(tag1, tag2, src, "bgtz", (x, y) -> x > 0,
            (x, y, z, assembly) -> assembly.processBranchGreaterThanZero(x, y, z));
    }

    public static BranchElement getBlezElement(TagString tag1
            , TagString tag2, AbstractVariable src) {
        return new BranchElement(tag1, tag2, src, "blez", (x, y) -> x <= 0,
            (x, y, z, assembly) -> assembly.processBranchLessOrEqualZero(x, y, z));
    }

    public static BranchElement getBltzElement(TagString tag1
            , TagString tag2, AbstractVariable src) {
        return new BranchElement(tag1, tag2, src, "bltz", (x, y) -> x < 0,
            (x, y, z, assembly) -> assembly.processBranchLessThanZero(x, y, z));
    }

    public static CalculateElement getAddElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "add", Integer::sum,
            (x, y, z, assembly) -> assembly.processAdd(x, y, z));
    }

    public static CalculateElement getSubElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "sub", (x, y) -> x - y,
            (x, y, z, assembly) -> assembly.processSub(x, y, z));
    }

    public static CalculateElement getMulElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "mul", (x, y) -> x * y,
            (x, y, z, assembly) -> assembly.processMul(x, y, z));
    }

    public static CalculateElement getDivElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "div", (x, y) -> x / y,
            (x, y, z, assembly) -> assembly.processDiv(x, y, z));
    }

    public static CalculateElement getModElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "mod", (x, y) -> x % y,
            (x, y, z, assembly) -> assembly.processMod(x, y, z));
    }

    public static CalculateElement getSllElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "sll", (x, y) -> x << y,
            (x, y, z, assembly) -> assembly.processSll(x, y, z));
    }

    public static CalculateElement getSeqElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "seq", (x, y) -> x.equals(y) ? 1 : 0,
            (x, y, z, assembly) -> assembly.processSetEqual(x, y, z));
    }

    public static CalculateElement getSneElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "sne", (x, y) -> x.equals(y) ? 0 : 1,
            (x, y, z, assembly) -> assembly.processSetNotEqual(x, y, z));
    }

    public static CalculateElement getSgeElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "sge", (x, y) -> x >= y ? 1 : 0,
            (x, y, z, assembly) -> assembly.processSetGreatEqual(x, y, z));
    }

    public static CalculateElement getSgtElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "sgt", (x, y) -> x > y ? 1 : 0,
            (x, y, z, assembly) -> assembly.processSetGreatThan(x, y, z));
    }

    public static CalculateElement getSleElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "sle", (x, y) -> x <= y ? 1 : 0,
            (x, y, z, assembly) -> assembly.processLessEqual(x, y, z));
    }

    public static CalculateElement getSltElement(AbstractVariable dst
            , AbstractVariable src1, AbstractVariable src2) {
        return new CalculateElement(dst, src1, src2, "slt", (x, y) -> x < y ? 1 : 0,
            (x, y, z, assembly) -> assembly.processLessThan(x, y, z));
    }

    public static MemoryElement getLoadElement(AbstractVariable dst, AbstractVariable addr) {
        return MemoryElement.createLoadElement(dst, addr);
    }

    public static MemoryElement getStoreElement(AbstractVariable dst, AbstractVariable addr) {
        return MemoryElement.createStoreElement(dst, addr);
    }

    public static MemoryElement getAllocElement(AbstractVariable dst, AbstractVariable size) {
        return MemoryElement.createAllocElement(dst, size);
    }

    public static SimpleElement getDeclElement(AbstractVariable src) {
        return SimpleElement.createDeclElement(src);
    }

    public static SimpleElement getScanElement(AbstractVariable dst) {
        return SimpleFactory.createScanElement(dst);
    }

    public static SimpleElement getPutStrElement(AbstractVariable dst) {
        return SimpleFactory.createPutStrElement(dst);
    }

    public static SimpleElement getPutNumElement(AbstractVariable dst) {
        return SimpleFactory.createPutNumElement(dst);
    }

    public static SimpleElement getParamElement(AbstractVariable operand) {
        return SimpleFactory.createParamElement(operand);
    }

    public static SimpleElement getCallElement(Function operand) {
        return SimpleFactory.createCallElement(operand);
    }

    public static SimpleElement getExitElement(AbstractVariable operand) {
        return SimpleFactory.createExitElement(operand);
    }

    public static SimpleElement getJumpElement(TagString operand) {
        return SimpleElement.createJumpElement(operand);
    }

    public static SimpleElement getTagElement(AbstractOperand operand) {
        return SimpleElement.createTagElement(operand);
    }

    protected void putLeftValueOnly(HashSet<AbstractLeftValue> variableSet
            , AbstractOperand... variables) {
        for (AbstractOperand i : variables) {
            if (i instanceof AbstractLeftValue) {
                variableSet.add((AbstractLeftValue)i);
            }
        }
    }

    protected void updateVariableWeight(AbstractOperand... variables) {
        HashSet<AbstractLeftValue> unique = new HashSet<>();
        for (AbstractOperand i : variables) {
            if (i instanceof AbstractLeftValue) {
                unique.add((AbstractLeftValue)i);
            }
        }
        unique.forEach(i -> i.updateWeight(loopLevel));
    }

    protected int index;
    protected int loopLevel;
    protected final String name;
    protected BasicBlock basicBlock;

    protected AbstractElement(String name) {
        this.name = name;
        this.loopLevel = 0;
    }

    public String getName() {
        return name;
    }

    public void setLoopLevel(int loopLevel) {
        this.loopLevel = loopLevel;
    }

    public void setPlaceInfo(BasicBlock basicBlock, int index) {
        this.index = index;
        this.basicBlock = basicBlock;
    }

    public abstract AbstractOperand getMain();

    public abstract void pushUpUse(HashSet<AbstractLeftValue> useSet);

    public abstract void pushUpDef(HashSet<AbstractLeftValue> defSet);

    public abstract void replaceVariable(AbstractLeftValue src, AbstractVariable replace);

    public abstract void countVariableUsage();

    public abstract void execute(VirtualRunner runner);

    public abstract int getSpace();

    public abstract void setAddress(int address);

    public abstract void toMipsAssembly(MipsAssembly assembly);

    public abstract String displayExecute(VirtualRunner runner);
}