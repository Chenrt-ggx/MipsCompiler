package IntermediateCode;

import Global.Settings;
import java.util.ArrayList;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.BranchElement;
import IntermediateCode.InterElement.AbstractElement;
import IntermediateCode.InterElement.CalculateElement;

public class BranchOptimizeTemplate {
    protected static void optimize(ArrayList<AbstractElement> intermediates) {
        if (Settings.branchInstructionOptimize && intermediates.size() >= 2
                && intermediates.get(intermediates.size() - 1).getName().equals("bnz")) {
            TagString[] tags = ((BranchElement)intermediates.get(intermediates.size() - 1)).getTags();
            AbstractElement abstractElement = intermediates.get(intermediates.size() - 2);
            switch (abstractElement.getName()) {
                case "seq": {
                    AbstractVariable[] operands = ((CalculateElement) intermediates
                            .get(intermediates.size() - 2)).getOperands();
                    intermediates.remove(intermediates.size() - 1);
                    intermediates.remove(intermediates.size() - 1);
                    intermediates.add(AbstractElement.getBeqElement(tags[0], tags[1], operands[0], operands[1]));
                    break;
                }
                case "sne": {
                    AbstractVariable[] operands = ((CalculateElement) intermediates
                            .get(intermediates.size() - 2)).getOperands();
                    intermediates.remove(intermediates.size() - 1);
                    intermediates.remove(intermediates.size() - 1);
                    intermediates.add(AbstractElement.getBneElement(tags[0], tags[1], operands[0], operands[1]));
                    break;
                }
                case "sgt": {
                    AbstractVariable[] operands = ((CalculateElement) intermediates
                            .get(intermediates.size() - 2)).getOperands();
                    if (operands[0] instanceof ConstValue && ((ConstValue) operands[0]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBltzElement(tags[0], tags[1], operands[1]));
                    }
                    else if (operands[1] instanceof ConstValue && ((ConstValue) operands[1]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBgtzElement(tags[0], tags[1], operands[0]));
                    }
                    break;
                }
                case "sge": {
                    AbstractVariable[] operands = ((CalculateElement) intermediates
                            .get(intermediates.size() - 2)).getOperands();
                    if (operands[0] instanceof ConstValue && ((ConstValue) operands[0]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBlezElement(tags[0], tags[1], operands[1]));
                    }
                    else if (operands[1] instanceof ConstValue && ((ConstValue) operands[1]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBgezElement(tags[0], tags[1], operands[0]));
                    }
                    break;
                }
                case "slt": {
                    AbstractVariable[] operands = ((CalculateElement) intermediates
                            .get(intermediates.size() - 2)).getOperands();
                    if (operands[0] instanceof ConstValue && ((ConstValue) operands[0]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBgtzElement(tags[0], tags[1], operands[1]));
                    }
                    else if (operands[1] instanceof ConstValue && ((ConstValue) operands[1]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBltzElement(tags[0], tags[1], operands[0]));
                    }
                    break;
                }
                case "sle": {
                    AbstractVariable[] operands = ((CalculateElement) intermediates
                            .get(intermediates.size() - 2)).getOperands();
                    if (operands[0] instanceof ConstValue && ((ConstValue) operands[0]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBgezElement(tags[0], tags[1], operands[1]));
                    }
                    else if (operands[1] instanceof ConstValue && ((ConstValue) operands[1]).getValue() == 0) {
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.remove(intermediates.size() - 1);
                        intermediates.add(AbstractElement.getBlezElement(tags[0], tags[1], operands[0]));
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }
}