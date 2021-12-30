package IntermediateCode.Operands;

public abstract class AbstractOperand {
    protected final String operand;

    public AbstractOperand(String operand) {
        this.operand = operand;
    }

    public String operand() {
        return operand;
    }

    public abstract int getSpace();

    @Override
    public int hashCode() {
        return operand.hashCode();
    }

    @Override
    public String toString() {
        return operand;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractOperand)) {
            return false;
        }
        return operand.equals(((AbstractOperand)obj).operand);
    }
}