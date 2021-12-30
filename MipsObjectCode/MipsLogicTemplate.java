package MipsObjectCode;

import java.util.ArrayList;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsLogicTemplate {
    private static void shorterSeqTemplate(int srcRegister, int value
            , ArrayList<AbstractInstruction> objectCode) {
        if (-Short.MAX_VALUE <= value && value <= -Short.MIN_VALUE) {
            objectCode.add(AbstractInstruction.getAddiu(MipsAssembly.at, srcRegister, -value));
        }
        else if (0 <= value && value <= Short.MAX_VALUE - Short.MIN_VALUE) {
            objectCode.add(AbstractInstruction.getXori(MipsAssembly.at, srcRegister, value));
        }
        else {
            MipsCalTemplate.initTemplate(MipsAssembly.at, value, objectCode);
            objectCode.add(AbstractInstruction.getXor(MipsAssembly.at, srcRegister, MipsAssembly.at));
        }
    }

    protected static void seqTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, (((ConstValue)src1).getValue()
                    == ((ConstValue)src2).getValue()) ? 1 : 0, objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            MipsLogicTemplate.shorterSeqTemplate(srcRegister, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSltiu(dstRegister, MipsAssembly.at, 1));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            MipsLogicTemplate.shorterSeqTemplate(srcRegister, ((ConstValue)src2).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSltiu(dstRegister, MipsAssembly.at, 1));
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getXor(MipsAssembly.at, srcRegister1, srcRegister2));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getSltiu(dstRegister, MipsAssembly.at, 1));
        }
    }

    protected static void sneTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, ((ConstValue)src1).getValue()
                    != (((ConstValue)src2).getValue()) ? 1 : 0, objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            MipsLogicTemplate.shorterSeqTemplate(srcRegister, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSltu(dstRegister, MipsAssembly.zero, MipsAssembly.at));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            MipsLogicTemplate.shorterSeqTemplate(srcRegister, ((ConstValue)src2).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSltu(dstRegister, MipsAssembly.zero, MipsAssembly.at));
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getXor(MipsAssembly.at, srcRegister1, srcRegister2));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getSltu(dstRegister, MipsAssembly.zero, MipsAssembly.at));
        }
    }

    protected static void sleTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, (((ConstValue)src1).getValue()
                    <= ((ConstValue)src2).getValue()) ? 1 : 0, objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            int constValue = ((ConstValue)src1).getValue();
            if (Short.MIN_VALUE <= constValue && constValue <= Short.MAX_VALUE) {
                objectCode.add(AbstractInstruction.getSlti(MipsAssembly.at, srcRegister, constValue));
            }
            else {
                MipsCalTemplate.initTemplate(MipsAssembly.at, constValue, objectCode);
                objectCode.add(AbstractInstruction.getSlt(MipsAssembly.at, srcRegister, MipsAssembly.at));
            }
            objectCode.add(AbstractInstruction.getXori(dstRegister, MipsAssembly.at, 1));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src2).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSlt(MipsAssembly.at, MipsAssembly.at, srcRegister));
            objectCode.add(AbstractInstruction.getXori(dstRegister, MipsAssembly.at, 1));
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getSlt(MipsAssembly.at, srcRegister2, srcRegister1));
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getXori(dstRegister, MipsAssembly.at, 1));
        }
    }

    protected static void sltTemplate(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(dstRegister, (((ConstValue)src1).getValue()
                    < ((ConstValue)src2).getValue()) ? 1 : 0, objectCode);
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getSlt(dstRegister, MipsAssembly.at, srcRegister));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            int constValue = ((ConstValue)src2).getValue();
            if (Short.MIN_VALUE <= constValue && constValue <= Short.MAX_VALUE) {
                objectCode.add(AbstractInstruction.getSlti(dstRegister, srcRegister, constValue));
            }
            else {
                MipsCalTemplate.initTemplate(MipsAssembly.at, constValue, objectCode);
                objectCode.add(AbstractInstruction.getSlt(dstRegister, srcRegister, MipsAssembly.at));
            }
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            int dstRegister = dst.appointRegister(assembly);
            objectCode.add(AbstractInstruction.getSlt(dstRegister, srcRegister1, srcRegister2));
        }
    }

    protected static void branchNotZeroTemplate(AbstractVariable src, String thenTag, String elseTag
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(AbstractInstruction.getJump(((ConstValue)src)
                    .getValue() != 0 ? thenTag : elseTag));
        }
        else {
            int srcRegister = src.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getBne(srcRegister, MipsAssembly.zero, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
    }

    protected static void branchNotEqualTemplate(AbstractVariable src1, AbstractVariable src2
            , String thenTag, String elseTag, ArrayList<AbstractInstruction> objectCode
            , MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            objectCode.add(AbstractInstruction.getJump(((ConstValue)src1).getValue()
                    != ((ConstValue)src2).getValue() ? thenTag : elseTag));
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getBne(MipsAssembly.at, srcRegister, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src2).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getBne(srcRegister, MipsAssembly.at, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getBne(srcRegister1, srcRegister2, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
    }

    protected static void branchIfEqualTemplate(AbstractVariable src1, AbstractVariable src2
            , String thenTag, String elseTag, ArrayList<AbstractInstruction> objectCode
            , MipsAssembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            objectCode.add(AbstractInstruction.getJump(((ConstValue)src1).getValue()
                    == ((ConstValue)src2).getValue() ? thenTag : elseTag));
        }
        else if (src1 instanceof ConstValue) {
            int srcRegister = src2.loadToRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src1).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getBeq(MipsAssembly.at, srcRegister, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
        else if (src2 instanceof ConstValue) {
            int srcRegister = src1.loadToRegister(assembly);
            MipsCalTemplate.initTemplate(MipsAssembly.at, ((ConstValue)src2).getValue(), objectCode);
            objectCode.add(AbstractInstruction.getBeq(srcRegister, MipsAssembly.at, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
        else {
            int srcRegister1 = src1.loadToRegister(assembly);
            int srcRegister2 = src2.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getBeq(srcRegister1, srcRegister2, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
    }

    protected static void branchGreaterOrEqualZeroTemplate(AbstractVariable src, String thenTag, String elseTag
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(AbstractInstruction.getJump(((ConstValue)src)
                    .getValue() >= 0 ? thenTag : elseTag));
        }
        else {
            int srcRegister = src.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getBgez(srcRegister, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
    }

    protected static void branchGreaterThanTemplate(AbstractVariable src, String thenTag, String elseTag
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(AbstractInstruction.getJump(((ConstValue)src)
                    .getValue() > 0 ? thenTag : elseTag));
        }
        else {
            int srcRegister = src.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getBgtz(srcRegister, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
    }

    protected static void branchLessOrEqualZeroTemplate(AbstractVariable src, String thenTag, String elseTag
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(AbstractInstruction.getJump(((ConstValue)src)
                    .getValue() <= 0 ? thenTag : elseTag));
        }
        else {
            int srcRegister = src.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getBlez(srcRegister, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
    }

    protected static void branchLessThanZeroTemplate(AbstractVariable src, String thenTag, String elseTag
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(AbstractInstruction.getJump(((ConstValue)src)
                    .getValue() < 0 ? thenTag : elseTag));
        }
        else {
            int srcRegister = src.loadToRegister(assembly);
            objectCode.add(AbstractInstruction.getBltz(srcRegister, thenTag));
            objectCode.add(AbstractInstruction.getJump(elseTag));
        }
    }
}