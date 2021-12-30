package MipsObjectCode;

import java.util.ArrayList;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.GlobalVariable;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsMemTemplate {
    protected static void loadRegisterTemplate(int dstRegister, int offset, int base
            , ArrayList<AbstractInstruction> objectCode) {
        objectCode.add(AbstractInstruction.getLoad(dstRegister, offset << 2, base));
    }

    protected static void storeRegisterTemplate(int dstRegister, int offset, int base
            , ArrayList<AbstractInstruction> objectCode) {
        objectCode.add(AbstractInstruction.getStore(dstRegister, offset << 2, base));
    }

    protected static void loadAddrTemplate(AbstractLeftValue dst, int offset
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        int realOffset = offset << 2;
        int dstRegister = dst.appointRegister(assembly);
        MipsCalTemplate.addValueTemplate(dstRegister, dst instanceof GlobalVariable
                ? MipsAssembly.gp : MipsAssembly.sp, realOffset, objectCode);
    }

    protected static void loadWordTemplate(AbstractLeftValue dst, AbstractVariable addr
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (addr instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            int constValue = ((ConstValue)addr).getValue();
            if (Short.MIN_VALUE <= constValue && constValue <= Short.MAX_VALUE) {
                objectCode.add(AbstractInstruction.getLoad(dstRegister, constValue, MipsAssembly.zero));
            }
            else {
                int offset = constValue % (Short.MAX_VALUE - Short.MIN_VALUE);
                if (offset > Short.MAX_VALUE) {
                    offset -= (Short.MAX_VALUE - Short.MIN_VALUE);
                }
                MipsCalTemplate.initTemplate(MipsAssembly.at, constValue - offset, objectCode);
                objectCode.add(AbstractInstruction.getLoad(dstRegister, offset, MipsAssembly.at));
            }
        }
        else {
            int addrRegister = addr.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getLoad(dstRegister, 0, addrRegister));
        }
    }

    protected static void storeWordTemplate(AbstractVariable src, AbstractVariable addr
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src instanceof ConstValue) {
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src).getValue(), objectCode);
        }
        int srcRegister = src instanceof ConstValue ? MipsAssembly.at : src.loadToRegister(assembly);
        if (addr instanceof ConstValue) {
            int constValue = ((ConstValue)addr).getValue();
            if (Short.MIN_VALUE <= constValue && constValue <= Short.MAX_VALUE) {
                objectCode.add(AbstractInstruction.getStore(srcRegister, constValue, MipsAssembly.zero));
            }
            else {
                int offset = constValue % (Short.MAX_VALUE - Short.MIN_VALUE);
                if (offset > Short.MAX_VALUE) {
                    offset -= (Short.MAX_VALUE - Short.MIN_VALUE);
                }
                MipsCalTemplate.initTemplate(MipsAssembly.at, constValue - offset, objectCode);
                objectCode.add(AbstractInstruction.getStore(srcRegister, offset, MipsAssembly.at));
            }
        }
        else {
            int addrRegister = addr.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getStore(srcRegister, 0, addrRegister));
        }
    }
}