package MipsObjectCode.InstructionSet;

public class AbstractMove extends AbstractInstruction {
    private final int dst;
    private final int src;

    protected AbstractMove(String name, int dst, int src) {
        super(name);
        this.dst = dst;
        this.src = src;
    }

    @Override
    public String toString() {
        return name + " $" + dst + (src >= 0 ? (", $" + src) : "");
    }
}