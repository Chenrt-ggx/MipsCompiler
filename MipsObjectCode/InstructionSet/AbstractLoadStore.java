package MipsObjectCode.InstructionSet;

public class AbstractLoadStore extends AbstractInstruction {
    private final int reg;
    private final int addr;
    private final int offset;

    protected AbstractLoadStore(String name, int reg, int offset, int addr) {
        super(name);
        this.reg = reg;
        this.addr = addr;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return name + " $" + reg + ", " + offset + "($" + addr + ")";
    }
}