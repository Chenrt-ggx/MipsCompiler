package MipsObjectCode.InstructionSet;

public class AbstractOther extends AbstractInstruction {
    @FunctionalInterface
    protected interface DisplayAction {
        String accept(String name, String tag, int value1, int value2);
    }

    private final int value1;
    private final int value2;
    private final String tag;
    private final DisplayAction displayAction;

    protected AbstractOther(String name, int value1, int value2, String tag, DisplayAction displayAction) {
        super(name);
        this.tag = tag;
        this.value1 = value1;
        this.value2 = value2;
        this.displayAction = displayAction;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return displayAction.accept(name, tag, value1, value2);
    }
}