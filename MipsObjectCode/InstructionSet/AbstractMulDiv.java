package MipsObjectCode.InstructionSet;

public class AbstractMulDiv extends AbstractInstruction {
    private final int src1;
    private final int src2;

    protected AbstractMulDiv(String name, int src1, int src2) {
        super(name);
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String toString() {
        return name + " $" + src1 + ", $" + src2;
    }
}