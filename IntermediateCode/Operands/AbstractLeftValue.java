package IntermediateCode.Operands;

import Global.Pair;
import Global.Settings;
import IntermediateCode.BasicBlock;
import MipsObjectCode.MipsAssembly;

public abstract class AbstractLeftValue extends AbstractVariable
        implements Comparable<AbstractLeftValue> {
    protected int index;
    protected int weight;
    protected int offset;
    protected int register;
    protected int bufRegister;
    protected BasicBlock basicBlock;

    protected final int variableId;
    protected final int variableType;

    public AbstractLeftValue(int variableType, int variableId) {
        super(variableType, variableId);
        this.weight = 0;
        this.variableId = variableId;
        this.variableType = variableType;
        this.bufRegister = this.register = this.offset = -1;
    }

    @Override
    public int getSpace() {
        return offset < 0 ? 1 : 0;
    }

    public int getRegister() {
        return register;
    }

    public int getIndex() {
        return index;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public void setPlaceInfo(BasicBlock basicBlock, int index) {
        this.index = index;
        this.basicBlock = basicBlock;
    }

    public void updateWeight(int loopLevel) {
        weight += 1 << loopLevel;
    }

    public void updateConflicts(int count) {
        weight = (weight + count) / (count + 1);
    }

    public abstract void writeBack(MipsAssembly assembly);

    public abstract void updateVariable(MipsAssembly assembly);

    public abstract void pushVariable(MipsAssembly assembly);

    public abstract void popVariable(MipsAssembly assembly);

    public void setAddrOffset(int addrOffset) {
        if (offset < 0) {
            offset = addrOffset;
        }
    }

    public int appointRegister(MipsAssembly assembly) {
        if (register > 0) {
            return register;
        }
        bufRegister = assembly.processReplaceRegister(this).getValue();
        return bufRegister;
    }

    protected int loadToRegisterAtBase(MipsAssembly assembly, int base) {
        if (register > 0) {
            return register;
        }
        else {
            Pair<Boolean, Integer> info = assembly.processReplaceRegister(this);
            bufRegister = info.getValue();
            if (!Settings.optTempRegisterOptimize || info.getKey()) {
                assembly.processLoadRegister(bufRegister, offset, base);
            }
            return bufRegister;
        }
    }

    protected void loadToRegisterAtBase(int dst, MipsAssembly assembly, int base) {
        if (register > 0) {
            assembly.processMove(dst, register);
        }
        else {
            if (Settings.optTempRegisterOptimize && bufRegister > 0
                    && assembly.processCheckValid(this)) {
                assembly.processMove(dst, bufRegister);
            }
            else {
                assembly.processLoadRegister(dst, offset, base);
            }
        }
    }

    protected void writeBackAtBase(MipsAssembly assembly, int base) {
        if (register < 0) {
            assembly.processStoreRegister(bufRegister, offset, base);
        }
    }

    protected void updateVariableAtBase(MipsAssembly assembly, int base) {
        if (register < 0) {
            if (!Settings.optTempRegisterOptimize) {
                assembly.processStoreRegister(bufRegister, offset, base);
                assembly.processRemoveRegister(this);
            }
            else {
                assembly.processMarkRegister(this);
            }
        }
    }

    protected void saveAtBase(MipsAssembly assembly) {
        if (register > 0) {
            assembly.processStoreRegister(register, offset, MipsAssembly.sp);
        }
    }

    protected void loadAtBase(MipsAssembly assembly) {
        if (register > 0) {
            assembly.processLoadRegister(register, offset, MipsAssembly.sp);
        }
    }

    @Override
    public int compareTo(AbstractLeftValue rhs) {
        if (weight != rhs.weight) {
            return Integer.compare(weight, rhs.weight);
        }
        if (variableType != rhs.variableType) {
            return Integer.compare(variableType, rhs.variableType);
        }
        return Integer.compare(variableId, rhs.variableId);
    }

    @Override
    public int hashCode() {
        return (variableType + 114) * (variableId + 514);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractLeftValue)) {
            return false;
        }
        AbstractLeftValue var = (AbstractLeftValue)obj;
        return this.variableId == var.variableId && this.variableType == var.variableType;
    }
}