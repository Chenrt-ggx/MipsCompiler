package MipsObjectCode;

import java.util.ArrayList;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsFuncTemplate {
    protected static void scanTemplate(AbstractLeftValue dst
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        int dstRegister = dst.appointRegister(assembly);
        MipsCalTemplate.initTemplate(MipsAssembly.v0, 5, objectCode);
        objectCode.add(AbstractInstruction.getSyscall());
        MipsCalTemplate.moveTemplate(dstRegister, MipsAssembly.v0, objectCode);
    }

    protected static void printStrTemplate(ConstValue address, ArrayList<AbstractInstruction> objectCode) {
        objectCode.add(AbstractInstruction.getLoadTag(MipsAssembly.a0, "str_" + address.getValue()));
        MipsCalTemplate.initTemplate(MipsAssembly.v0, 4, objectCode);
        objectCode.add(AbstractInstruction.getSyscall());
    }

    protected static void printNumTemplate(AbstractVariable variable
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (variable instanceof ConstValue) {
            MipsCalTemplate.initTemplate(MipsAssembly.a0,
                    ((ConstValue)variable).getValue(), objectCode);
        }
        else {
            variable.loadToRegister(MipsAssembly.a0, assembly);
        }
        MipsCalTemplate.initTemplate(MipsAssembly.v0, 1, objectCode);
        objectCode.add(AbstractInstruction.getSyscall());
    }

    protected static void funcParamTemplate(AbstractVariable variable
            , ArrayList<AbstractVariable> funcParams) {
        funcParams.add(variable);
    }

    protected static void funcCallTemplate(Function function, ArrayList<AbstractVariable> funcParams
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (function.isMain()) {
            if (function.getSpace() != 0) {
                MipsCalTemplate.addValueTemplate(MipsAssembly.sp, MipsAssembly.sp
                        , -function.getSpace() << 2, objectCode);
            }
            objectCode.add(AbstractInstruction.getJump(function.operand()));
        }
        else {
            int space = function.getSpace() << 2;
            objectCode.add(AbstractInstruction.getStore(MipsAssembly.ra, 0, MipsAssembly.sp));
            for (int i = 0; i < funcParams.size(); i++) {
                if (funcParams.get(i) instanceof ConstValue) {
                    MipsCalTemplate.initTemplate(MipsAssembly.at,
                            ((ConstValue)funcParams.get(i)).getValue(), objectCode);
                }
                int paramRegister = funcParams.get(i) instanceof ConstValue ?
                        MipsAssembly.at : funcParams.get(i).loadToRegister(assembly);
                objectCode.add(AbstractInstruction.getStore(paramRegister,
                        ((i + 1) << 2) - space, MipsAssembly.sp));
            }
            funcParams.clear();
            MipsCalTemplate.addValueTemplate(MipsAssembly.sp, MipsAssembly.sp, -space, objectCode);
            objectCode.add(AbstractInstruction.getJumpLink(function.operand()));
            MipsCalTemplate.addValueTemplate(MipsAssembly.sp, MipsAssembly.sp, space, objectCode);
            MipsMemTemplate.loadRegisterTemplate(MipsAssembly.ra, 0, MipsAssembly.sp, objectCode);
        }
    }

    protected static void funcReturnTemplate(Function function, AbstractVariable variable
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (function.isMain()) {
            MipsCalTemplate.initTemplate(MipsAssembly.v0, 10, objectCode);
            objectCode.add(AbstractInstruction.getSyscall());
        }
        else if (variable == null) {
            objectCode.add(AbstractInstruction.getJumpRegister(MipsAssembly.ra));
        }
        else {
            if (variable instanceof ConstValue) {
                MipsCalTemplate.initTemplate(MipsAssembly.v0,
                        ((ConstValue)variable).getValue(), objectCode);
            }
            else {
                variable.loadToRegister(MipsAssembly.v0, assembly);
            }
            objectCode.add(AbstractInstruction.getJumpRegister(MipsAssembly.ra));
        }
    }
}