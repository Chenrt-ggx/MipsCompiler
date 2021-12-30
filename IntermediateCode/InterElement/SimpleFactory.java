package IntermediateCode.InterElement;

import Global.Settings;
import java.util.HashSet;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.VirtualRunner;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.AbstractOperand;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public class SimpleFactory {
    protected static SimpleElement createScanElement(AbstractVariable dst) {
        return new SimpleElement(dst, "scan", true) {
            @Override
            protected void pushUpDef(AbstractOperand operand, HashSet<AbstractLeftValue> defSet) {
                defSet.add((AbstractLeftValue)operand);
            }

            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                runner.setVariable((AbstractVariable)operand, runner.readInt());
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
                assembly.processScan((AbstractLeftValue)operand);
                ((AbstractLeftValue)operand).updateVariable(assembly);
            }
        };
    }

    protected static SimpleElement createPutStrElement(AbstractVariable dst) {
        return new SimpleElement(dst, "puts", false) {
            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                runner.printString("" + runner.getString(
                        runner.getVariable((ConstValue)operand)));
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                assembly.processPutString((ConstValue)operand);
            }
        };
    }

    protected static SimpleElement createPutNumElement(AbstractVariable dst) {
        return new SimpleElement(dst, "putn", false) {
            @Override
            protected boolean acceptSpread() {
                return true;
            }

            @Override
            protected void pushUpUse(AbstractOperand operand, HashSet<AbstractLeftValue> useSet) {
                putLeftValueOnly(useSet, operand);
            }

            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                runner.printString("" + runner.getVariable((AbstractVariable)operand));
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                assembly.processPutNumber((AbstractVariable)operand);
            }
        };
    }

    protected static SimpleElement createParamElement(AbstractVariable operand) {
        return new SimpleElement(operand, "push", false) {
            @Override
            protected boolean acceptSpread() {
                return true;
            }

            @Override
            protected void pushUpUse(AbstractOperand operand, HashSet<AbstractLeftValue> useSet) {
                putLeftValueOnly(useSet, operand);
            }

            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                runner.pushParam((AbstractVariable)operand);
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                assembly.processParam((AbstractVariable)operand);
            }
        };
    }

    protected static SimpleElement createCallElement(Function operand) {
        return new SimpleElement(operand, "call", false) {
            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                runner.callFunc((Function)operand);
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                ((Function)operand).getActiveVariables(basicBlock, index)
                        .forEach(i -> i.pushVariable(assembly));
                if (Settings.optTempRegisterOptimize) {
                    assembly.processFlushLocalRegister(
                            ((Function)operand).getActiveVariables(basicBlock, index));
                }
                assembly.processCallFunc((Function)operand);
                if (Settings.optTempRegisterOptimize) {
                    assembly.processClearLocalRegister();
                }
                ((Function)operand).getActiveVariables(basicBlock, index)
                        .forEach(i -> i.popVariable(assembly));
            }
        };
    }

    protected static SimpleElement createExitElement(AbstractVariable operand) {
        return new SimpleElement(operand, "exit", false) {
            @Override
            protected boolean acceptSpread() {
                return true;
            }

            @Override
            protected void pushUpUse(AbstractOperand operand, HashSet<AbstractLeftValue> useSet) {
                putLeftValueOnly(useSet, operand);
            }

            @Override
            protected void execute(VirtualRunner runner, AbstractOperand operand) {
                boolean hasReturn = operand != null;
                runner.exitFunc(hasReturn ? runner.getVariable((AbstractVariable)operand) : 0, hasReturn);
            }

            @Override
            protected void toMipsAssembly(AbstractOperand operand, MipsAssembly assembly) {
                if (Settings.optTempRegisterOptimize) {
                    assembly.processFlushLocalRegister();
                }
                assembly.processBackFunc((AbstractVariable)operand);
                if (Settings.optTempRegisterOptimize) {
                    assembly.processClearLocalRegister();
                }
            }
        };
    }
}