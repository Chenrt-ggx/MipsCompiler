package MipsObjectCode.InstructionSet;

public class AbstractRegRegCal extends AbstractInstruction {
    private final int dst;
    private final int src1;
    private final int src2;

    protected AbstractRegRegCal(String name, int dst, int src1, int src2) {
        super(name);
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String toString() {
        return name + " $" + dst + ", $" + src1 + ", $" + src2;
    }
}