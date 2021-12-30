package IntermediateCode.Operands;

public class TagString extends AbstractOperand {
    public TagString(String operand) {
        super(operand);
    }

    @Override
    public int getSpace() {
        return 0;
    }
}