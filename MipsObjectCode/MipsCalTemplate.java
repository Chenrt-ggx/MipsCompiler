package MipsObjectCode;

import Global.Settings;
import java.util.ArrayList;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsCalTemplate {
    protected static void moveTemplate(int dstRegister, int srcRegister
            , ArrayList<AbstractInstruction> objectCode) {
        objectCode.add(AbstractInstruction.getXor(dstRegister, srcRegister, MipsAssembly.zero));
    }

    protected static void initTemplate(int dstRegister, int value
            , ArrayList<AbstractInstruction> objectCode) {
        if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            objectCode.add(AbstractInstruction.getAddiu(dstRegister, MipsAssembly.zero, value));
        }
        else if (0 <= value && value <= Short.MAX_VALUE - Short.MIN_VALUE) {
            objectCode.add(AbstractInstruction.getOri(dstRegister, MipsAssembly.zero, value));
        }
        else if ((value & 0xffff) == 0) {
            objectCode.add(AbstractInstruction.getLui(dstRegister, (value >> 16) & 0xffff));
        }
        else {
            objectCode.add(AbstractInstruction.getLui(dstRegister, (value >> 16) & 0xffff));
            objectCode.add(AbstractInstruction.getOri(dstRegister, dstRegister, value & 0xffff));
        }
    }

    protected static void addValueTemplate(int dstRegister, int srcRegister, int value
            , ArrayList<AbstractInstruction> objectCode) {
        if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            objectCode.add(AbstractInstruction.getAddiu(dstRegister, srcRegister, value));
        }
        else {
            MipsCalTemplate.initTemplate(MipsAssembly.at, value, objectCode);
            objectCode.add(AbstractInstruction.getAddu(dstRegister, srcRegister, MipsAssembly.at));
        }
    }

    protected static void addTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, ((ConstValue)src1).getValue()
                    + ((ConstValue)src2).getValue(), objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            addValueTemplate(dstRegister, srcRegister, ((ConstValue)src1).getValue(), objectCode);
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            addValueTemplate(dstRegister, srcRegister, ((ConstValue)src2).getValue(), objectCode);
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getAddu(dstRegister, srcRegister1, srcRegister2));
        }
    }

    protected static void subTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, ((ConstValue)src1).getValue()
                    - ((ConstValue)src2).getValue(), objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSubu(dstRegister, MipsAssembly.at, srcRegister));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            int constValue = ((ConstValue)src2).getValue();
            if (-Short.MAX_VALUE <= constValue && constValue <= -Short.MIN_VALUE) {
                objectCode.add(AbstractInstruction.getAddiu(dstRegister, srcRegister, -constValue));
            }
            else {
                MipsCalTemplate.initTemplate(MipsAssembly.at, constValue, objectCode);
                objectCode.add(AbstractInstruction.getSubu(dstRegister, srcRegister, MipsAssembly.at));
            }
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getSubu(dstRegister, srcRegister1, srcRegister2));
        }
    }

    protected static void mulTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, ((ConstValue)src1).getValue()
                    * ((ConstValue)src2).getValue(), objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int value = ((ConstValue)src1).getValue();
            if (Settings.mulDivCalculateOptimize && MipsMulDivOptimize.canOptimizeMul(value)) {
                MipsMulDivOptimize.mulCalOptimize(dst, src2, value, objectCode, assembly);
            }
            else {
                int srcRegister = src2.loadToRegister(assembly);
                MipsCalTemplate.initTemplate(MipsAssembly.at, value, objectCode);
                objectCode.add(AbstractInstruction.getMult(srcRegister, MipsAssembly.at));
                int dstRegister = dst.appointRegister(assembly);
                objectCode.add(AbstractInstruction.getMflo(dstRegister));
            }
        }
        else if (src2 instanceof ConstValue) {
            int value = ((ConstValue)src2).getValue();
            if (Settings.mulDivCalculateOptimize && MipsMulDivOptimize.canOptimizeMul(value)) {
                MipsMulDivOptimize.mulCalOptimize(dst, src1, value, objectCode, assembly);
            }
            else {
                int srcRegister = src1.loadToRegister(assembly);
                MipsCalTemplate.initTemplate(MipsAssembly.at, value, objectCode);
                objectCode.add(AbstractInstruction.getMult(srcRegister, MipsAssembly.at));
                int dstRegister = dst.appointRegister(assembly);
                objectCode.add(AbstractInstruction.getMflo(dstRegister));
            }
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getMult(srcRegister1, srcRegister2));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getMflo(dstRegister));
        }
    }

    protected static void divTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, ((ConstValue)src1).getValue()
                    / ((ConstValue)src2).getValue(), objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getDiv(srcRegister, MipsAssembly.at));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getMflo(dstRegister));
        }
        else if (src2 instanceof ConstValue) {
            int value = ((ConstValue)src2).getValue();
            if (Settings.mulDivCalculateOptimize) {
                MipsMulDivOptimize.divCalOptimize(dst, src1, value, objectCode, assembly);
            }
            else {
                int srcRegister = src1.loadToRegister(assembly);
                MipsCalTemplate.initTemplate(MipsAssembly.at, value, objectCode);
                objectCode.add(AbstractInstruction.getDiv(srcRegister, MipsAssembly.at));
                int dstRegister = dst.appointRegister(assembly);
                objectCode.add(AbstractInstruction.getMflo(dstRegister));
            }
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getDiv(srcRegister1, srcRegister2));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getMflo(dstRegister));
        }
    }

    protected static void modTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, ((ConstValue)src1).getValue()
                    % ((ConstValue)src2).getValue(), objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getDiv(MipsAssembly.at, srcRegister));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getMfhi(dstRegister));
        }
        else if (src2 instanceof ConstValue) {
            int value = ((ConstValue)src2).getValue();
            if (Settings.mulDivCalculateOptimize) {
                MipsMulDivOptimize.modCalOptimize(dst, src1, value, objectCode, assembly);
            }
            else {
                int srcRegister = src1.loadToRegister(assembly);
                MipsCalTemplate.initTemplate(MipsAssembly.at, value, objectCode);
                objectCode.add(AbstractInstruction.getDiv(srcRegister, MipsAssembly.at));
                int dstRegister = dst.appointRegister(assembly);
                objectCode.add(AbstractInstruction.getMfhi(dstRegister));
            }
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getDiv(srcRegister1, srcRegister2));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getMfhi(dstRegister));
        }
    }

    protected static void sllTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, ((ConstValue)src1).getValue()
                    << ((ConstValue)src2).getValue(), objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            initTemplate(MipsAssembly.at, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSllv(dstRegister, MipsAssembly.at, srcRegister));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getSll(dstRegister,
                    srcRegister, ((ConstValue)src2).getValue()));
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getSllv(dstRegister, srcRegister1, srcRegister2));
        }
    }
}