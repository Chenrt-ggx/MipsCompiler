package IntermediateCode.Operands;

import MipsObjectCode.MipsAssembly;

public class GlobalVariable extends AbstractLeftValue {
    public GlobalVariable(int id) {
        super(AbstractVariable.GLOBAL, id);
    }

    @Override
    public void writeBack(MipsAssembly assembly) {
        writeBackAtBase(assembly, MipsAssembly.gp);
    }

    @Override
    public void updateVariable(MipsAssembly assembly) {
        updateVariableAtBase(assembly, MipsAssembly.gp);
    }

    @Override
    public void pushVariable(MipsAssembly assembly) {
        /* Hey I like this so far! */
    }

    @Override
    public void popVariable(MipsAssembly assembly) {
        /* Hey I like this so far! */
    }

    @Override
    public int loadToRegister(MipsAssembly assembly) {
        return loadToRegisterAtBase(assembly, MipsAssembly.gp);
    }

    @Override
    public void loadToRegister(int dst, MipsAssembly assembly) {
        loadToRegisterAtBase(dst, assembly, MipsAssembly.gp);
    }
}