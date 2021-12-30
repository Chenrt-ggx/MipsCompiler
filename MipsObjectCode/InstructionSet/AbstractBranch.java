package MipsObjectCode.InstructionSet;

public class AbstractBranch extends AbstractInstruction {
    private final int src1;
    private final int src2;
    private final String tag;

    protected AbstractBranch(String name, int src, String tag) {
        super(name);
        this.tag = tag;
        this.src1 = src;
        this.src2 = -1;
    }

    protected AbstractBranch(String name, int src1, int src2, String tag) {
        super(name);
        this.tag = tag;
        this.src1 = src1;
        this.src2 = src2;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return name + " $" + src1 + (src2 >= 0 ? (", $" + src2) : "") + ", " + tag;
    }
}