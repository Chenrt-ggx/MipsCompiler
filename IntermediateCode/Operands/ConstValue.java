package IntermediateCode.Operands;

import MipsObjectCode.MipsAssembly;

public class ConstValue extends AbstractVariable {
    private final int value;

    public ConstValue(int value) {
        super(AbstractVariable.CONST, value);
        this.value = value;
    }

    @Override
    public int getSpace() {
        return 0;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int loadToRegister(MipsAssembly assembly) {
        System.err.println("unable to load const value to register");
        return -1;
    }

    @Override
    public void loadToRegister(int dst, MipsAssembly assembly) {
        System.err.println("unable to load const value to register");
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode() ^ value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ConstValue)) {
            return false;
        }
        ConstValue var = (ConstValue)obj;
        return this.value == var.value;
    }
}