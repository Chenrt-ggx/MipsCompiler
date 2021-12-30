package IntermediateCode.InterElement;

import java.util.HashSet;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.BasicBlock;
import IntermediateCode.VirtualRunner;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.AbstractOperand;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public abstract class MemoryElement extends AbstractElement {
    protected static MemoryElement createLoadElement(AbstractVariable dst, AbstractVariable addr) {
        return new MemoryElement(dst, addr, "load", true, false) {
            @Override
            protected void pushUpUse(AbstractVariable dst
                    , AbstractVariable addr, HashSet<AbstractLeftValue> useSet) {
                putLeftValueOnly(useSet, addr);
            }

            @Override
            protected void pushUpDef(AbstractVariable dst
                    , AbstractVariable addr, HashSet<AbstractLeftValue> defSet) {
                if (!dst.equals(addr)) {
                    defSet.add((AbstractLeftValue)dst);
                }
            }

            @Override
            protected void execute(AbstractVariable dst
                    , AbstractVariable addr, VirtualRunner runner) {
                runner.setVariable(dst, runner.getMemory(runner.getVariable(addr)));
            }

            @Override
            protected void setAddress(AbstractVariable dst, AbstractVariable addr, int address) {
                ((AbstractLeftValue)dst).setAddrOffset(address);
            }

            @Override
            protected int getSpace(AbstractVariable dst, AbstractVariable addr) {
                return dst.getSpace();
            }

            @Override
            protected void toMipsAssembly(AbstractVariable dst
                    , AbstractVariable addr, MipsAssembly assembly) {
                assembly.processLoadWord((AbstractLeftValue)dst, addr);
                ((AbstractLeftValue)dst).updateVariable(assembly);
            }
        };
    }

    protected static MemoryElement createStoreElement(AbstractVariable dst, AbstractVariable addr) {
        return new MemoryElement(dst, addr, "store", false, true) {
            @Override
            protected void pushUpUse(AbstractVariable dst
                    , AbstractVariable addr, HashSet<AbstractLeftValue> useSet) {
                putLeftValueOnly(useSet, dst, addr);
            }

            @Override
            protected void pushUpDef(AbstractVariable dst
                    , AbstractVariable addr, HashSet<AbstractLeftValue> defSet) {
                /* Hey I like this so far! */
            }

            @Override
            protected void execute(AbstractVariable dst
                    , AbstractVariable addr, VirtualRunner runner) {
                runner.setMemory(runner.getVariable(addr), runner.getVariable(dst));
            }

            @Override
            protected void setAddress(AbstractVariable dst, AbstractVariable addr, int address) {
                /* Hey I like this so far! */
            }

            @Override
            protected int getSpace(AbstractVariable dst, AbstractVariable addr) {
                return 0;
            }

            @Override
            protected void toMipsAssembly(AbstractVariable dst
                    , AbstractVariable addr, MipsAssembly assembly) {
                assembly.processStoreWord(dst, addr);
            }
        };
    }

    protected static MemoryElement createAllocElement(AbstractVariable dst, AbstractVariable size) {
        return new MemoryElement(dst, size, "alloc", true, false) {
            private int offset = 0;

            @Override
            protected void pushUpUse(AbstractVariable dst
                    , AbstractVariable size, HashSet<AbstractLeftValue> useSet) {
                /* Hey I like this so far! */
            }

            @Override
            protected void pushUpDef(AbstractVariable dst
                    , AbstractVariable size, HashSet<AbstractLeftValue> defSet) {
                defSet.add((AbstractLeftValue)dst);
            }

            @Override
            protected void execute(AbstractVariable dst
                    , AbstractVariable size, VirtualRunner runner) {
                runner.setVariable(dst, 0);
                runner.setVariable(dst, runner.allocMemory(runner.getVariable(size)));
            }

            @Override
            protected void setAddress(AbstractVariable dst, AbstractVariable size, int address) {
                offset = dst.getSpace() + address;
                ((AbstractLeftValue)dst).setAddrOffset(address);
            }

            @Override
            protected int getSpace(AbstractVariable dst, AbstractVariable size) {
                return ((ConstValue)size).getValue() + dst.getSpace();
            }

            @Override
            protected void toMipsAssembly(AbstractVariable dst
                    , AbstractVariable size, MipsAssembly assembly) {
                assembly.processLoadAddress((AbstractLeftValue)dst, offset);
                ((AbstractLeftValue)dst).updateVariable(assembly);
            }
        };
    }

    private AbstractVariable dst;
    private AbstractVariable addr;
    private final boolean spreadDst;

    protected MemoryElement(AbstractVariable dst, AbstractVariable addr
            , String name, boolean writeDst, boolean spreadDst) {
        super(name);
        this.addr = addr;
        this.spreadDst = spreadDst;
        if (!writeDst || dst instanceof AbstractLeftValue) {
            this.dst = dst;
        }
        else {
            System.err.println("unexpected write to uneditable variable");
            this.dst = null;
        }
    }

    public void replaceVariable(AbstractLeftValue src, AbstractVariable replace) {
        if (addr == src) {
            addr = replace;
        }
        if (spreadDst && dst == src) {
            dst = replace;
        }
    }

    protected abstract void pushUpUse(AbstractVariable dst
            , AbstractVariable addr, HashSet<AbstractLeftValue> useSet);

    @Override
    public void pushUpUse(HashSet<AbstractLeftValue> useSet) {
        pushUpUse(dst, addr, useSet);
    }

    protected abstract void pushUpDef(AbstractVariable dst
            , AbstractVariable addr, HashSet<AbstractLeftValue> defSet);

    @Override
    public void pushUpDef(HashSet<AbstractLeftValue> defSet) {
        pushUpDef(dst, addr, defSet);
    }

    @Override
    public void countVariableUsage() {
        updateVariableWeight(dst, addr);
    }

    protected abstract void execute(AbstractVariable dst
            , AbstractVariable addr, VirtualRunner runner);

    @Override
    public void execute(VirtualRunner runner) {
        execute(dst, addr, runner);
    }

    @Override
    public AbstractOperand getMain() {
        return dst;
    }

    protected abstract void setAddress(AbstractVariable dst, AbstractVariable addr, int address);

    @Override
    public void setAddress(int address) {
        setAddress(dst, addr, address);
    }

    protected abstract int getSpace(AbstractVariable dst, AbstractVariable addr);

    @Override
    public int getSpace() {
        return getSpace(dst, addr);
    }

    @Override
    public void setPlaceInfo(BasicBlock basicBlock, int index) {
        super.setPlaceInfo(basicBlock, index);
        if (dst instanceof AbstractLeftValue) {
            ((AbstractLeftValue)dst).setPlaceInfo(basicBlock, index);
        }
        if (addr instanceof AbstractLeftValue) {
            ((AbstractLeftValue)addr).setPlaceInfo(basicBlock, index);
        }
    }

    protected abstract void toMipsAssembly(AbstractVariable dst
            , AbstractVariable addr, MipsAssembly assembly);

    @Override
    public void toMipsAssembly(MipsAssembly assembly) {
        toMipsAssembly(dst, addr, assembly);
    }

    @Override
    public String displayExecute(VirtualRunner runner) {
        return name + " " + dst + "(" + runner.tryGetVariable(dst) + "), (" +
                addr + "(" + runner.tryGetVariable(addr) + " -> "
                + runner.tryGetMemory(runner.tryGetVariable(addr)) + "))";
    }

    @Override
    public String toString() {
        return name + " " + dst + ", (" + addr + ")";
    }
}