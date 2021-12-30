package IntermediateCode.Operands;

import MipsObjectCode.MipsAssembly;

public class ReturnValue extends AbstractVariable {
    public ReturnValue() {
        super(AbstractVariable.RETURN_VAL, -1);
    }

    @Override
    public int getSpace() {
        return 0;
    }

    @Override
    public int loadToRegister(MipsAssembly assembly) {
        return MipsAssembly.v0;
    }

    @Override
    public void loadToRegister(int dst, MipsAssembly assembly) {
        assembly.processMove(dst, MipsAssembly.v0);
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ReturnValue;
    }
}