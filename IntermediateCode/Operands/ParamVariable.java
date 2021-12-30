package IntermediateCode.Operands;

import MipsObjectCode.MipsAssembly;

public class ParamVariable extends AbstractLeftValue {
    public ParamVariable(int id) {
        super(AbstractVariable.PARAM, id);
        offset = id + 1;
    }

    @Override
    public void writeBack(MipsAssembly assembly) {
        writeBackAtBase(assembly, MipsAssembly.sp);
    }

    @Override
    public void updateVariable(MipsAssembly assembly) {
        updateVariableAtBase(assembly, MipsAssembly.sp);
    }

    @Override
    public void pushVariable(MipsAssembly assembly) {
        saveAtBase(assembly);
    }

    @Override
    public void popVariable(MipsAssembly assembly) {
        loadAtBase(assembly);
    }

    @Override
    public int loadToRegister(MipsAssembly assembly) {
        return loadToRegisterAtBase(assembly, MipsAssembly.sp);
    }

    @Override
    public void loadToRegister(int dst, MipsAssembly assembly) {
        loadToRegisterAtBase(dst, assembly, MipsAssembly.sp);
    }
}