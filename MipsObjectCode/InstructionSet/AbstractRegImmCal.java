package MipsObjectCode.InstructionSet;

public class AbstractRegImmCal extends AbstractInstruction {
    private final int dst;
    private final int src;
    private final int imm;

    protected AbstractRegImmCal(String name, int dst, int src, int imm) {
        super(name);
        this.dst = dst;
        this.src = src;
        this.imm = imm;
    }

    @Override
    public String toString() {
        return name + " $" + dst + ", $" + src + ", " + imm;
    }
}