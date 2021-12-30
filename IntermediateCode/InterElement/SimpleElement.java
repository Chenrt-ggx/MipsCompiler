package IntermediateCode.InterElement;

import Global.Settings;
import java.util.HashSet;
import IntermediateCode.BasicBlock;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.VirtualRunner;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.AbstractOperand;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public abstract class SimpleElement extends AbstractElement {
    protected static SimpleElement createDeclElement(AbstractVariable src) {
        return new SimpleElement(src, "decl", true) {
            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                runner.setVariable(src, 0);
            }

            @Override
            protected void setAddress(int address, AbstractOperand operand) {
                ((AbstractLeftValue)operand).setAddrOffset(address);
            }

            @Override
            protected int getSpace(AbstractOperand operand) {
                return operand.getSpace();
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                /* Hey I like this so far! */
            }
        };
    }

    protected static SimpleElement createJumpElement(TagString operand) {
        return new SimpleElement(operand, "jmp", false) {
            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                runner.processBranch((TagString)operand);
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                if (Settings.optTempRegisterOptimize) {
                    assembly.processFlushLocalRegister(basicBlock.getActiveOutSet());
                }
                assembly.processJump((TagString)operand);
                if (Settings.optTempRegisterOptimize) {
                    assembly.processClearLocalRegister();
                }
            }
        };
    }

    protected static SimpleElement createTagElement(AbstractOperand operand) {
        return new SimpleElement(operand, "tag", false) {
            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                /* Hey I like this so far! */
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                assembly.processTag(operand);
            }
        };
    }

    private AbstractOperand operand;

    protected SimpleElement(AbstractOperand operand, String name, boolean writeDst) {
        super(name);
        if (!writeDst || operand instanceof AbstractLeftValue) {
            this.operand = operand;
        }
        else {
            System.err.println("unexpected write to uneditable variable");
            this.operand = null;
        }
    }

    protected boolean acceptSpread() {
        return false;
    }

    @Override
    public void replaceVariable(AbstractLeftValue src, AbstractVariable replace) {
        if (acceptSpread() && operand == src) {
            operand = replace;
        }
    }

    protected void pushUpUse(AbstractOperand operand, HashSet<AbstractLeftValue> useSet) {
        /* Hey I like this so far! */
    }

    @Override
    public void pushUpUse(HashSet<AbstractLeftValue> useSet) {
        pushUpUse(operand, useSet);
    }

    protected void pushUpDef(AbstractOperand operand, HashSet<AbstractLeftValue> defSet) {
        /* Hey I like this so far! */
    }

    @Override
    public void pushUpDef(HashSet<AbstractLeftValue> defSet) {
        pushUpDef(operand, defSet);
    }

    @Override
    public void countVariableUsage() {
        updateVariableWeight(operand);
    }

    protected abstract void execute(VirtualRunner runner, AbstractOperand operand);

    @Override
    public void execute(VirtualRunner runner) {
        execute(runner, operand);
    }

    @Override
    public AbstractOperand getMain() {
        return operand;
    }

    protected void setAddress(int address, AbstractOperand operand) {
        /* Hey I like this so far! */
    }

    @Override
    public void setAddress(int address) {
        setAddress(address, operand);
    }

    protected int getSpace(AbstractOperand operand) {
        return 0;
    }

    @Override
    public int getSpace() {
        return getSpace(operand);
    }

    @Override
    public void setPlaceInfo(BasicBlock basicBlock, int index) {
        super.setPlaceInfo(basicBlock, index);
        if (operand instanceof AbstractLeftValue) {
            ((AbstractLeftValue)operand).setPlaceInfo(basicBlock, index);
        }
    }

    protected abstract void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly);

    @Override
    public void toMipsAssembly(MipsAssembly assembly) {
        toMipsAssembly(operand, assembly);
    }

    @Override
    public String displayExecute(VirtualRunner runner) {
        return toString();
    }

    @Override
    public String toString() {
        return name + " " + operand;
    }
}