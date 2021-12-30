package MipsObjectCode;

import Global.Pair;
import Global.Settings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.GlobalVariable;
import IntermediateCode.Operands.AbstractOperand;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsAssembly {
    public static final int zero = 0;
    public static final int at = 1;
    public static final int v0 = 2;
    public static final int v1 = 3;
    public static final int a0 = 4;
    public static final int gp = 28;
    public static final int sp = 29;
    public static final int ra = 31;

    public static int[] localRegisterPool = {
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    };
    public static int[] globalRegisterPool = {
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 30
    };
    public static final int[] registerPool = {
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 30
    };

    private boolean[] writeBackBuffer;
    private AbstractLeftValue[] abstractVariableBuffer;

    private int globalSpace;
    private Function currentFunction;
    private final ArrayList<Integer> dataField;
    private final LinkedList<String> initString;
    private final ArrayList<AbstractVariable> funcParams;
    private final ArrayList<AbstractInstruction> objectCode;

    public MipsAssembly(ArrayList<Integer> dataField) {
        globalSpace = 0;
        currentFunction = null;
        this.dataField = dataField;
        funcParams = new ArrayList<>();
        objectCode = new ArrayList<>();
        initString = new LinkedList<>();
        writeBackBuffer = new boolean[localRegisterPool.length];
        abstractVariableBuffer = new AbstractLeftValue[localRegisterPool.length];
        if (Settings.delayInjection) {
            MipsCalTemplate.initTemplate(MipsAssembly.a0, 8192, objectCode);
            objectCode.add(AbstractInstruction.getTag("DELAY_START_TAG"));
            objectCode.add(AbstractInstruction.getLoad(MipsAssembly.zero, 0, MipsAssembly.sp));
            objectCode.add(AbstractInstruction.getLoad(MipsAssembly.zero, 0, MipsAssembly.sp));
            objectCode.add(AbstractInstruction.getLoad(MipsAssembly.zero, 0, MipsAssembly.sp));
            objectCode.add(AbstractInstruction.getJump("IN_DELAY_TAG_1"));
            objectCode.add(AbstractInstruction.getTag("IN_DELAY_TAG_1"));
            objectCode.add(AbstractInstruction.getMult(MipsAssembly.zero, MipsAssembly.zero));
            objectCode.add(AbstractInstruction.getJump("IN_DELAY_TAG_2"));
            objectCode.add(AbstractInstruction.getTag("IN_DELAY_TAG_2"));
            objectCode.add(AbstractInstruction.getMult(MipsAssembly.sp, MipsAssembly.sp));
            objectCode.add(AbstractInstruction.getJump("IN_DELAY_TAG_3"));
            objectCode.add(AbstractInstruction.getTag("IN_DELAY_TAG_3"));
            objectCode.add(AbstractInstruction.getDiv(MipsAssembly.sp, MipsAssembly.sp));
            MipsCalTemplate.addValueTemplate(MipsAssembly.a0, MipsAssembly.a0, -1, objectCode);
            objectCode.add(AbstractInstruction.getBne(MipsAssembly.a0, MipsAssembly.zero, "DELAY_START_TAG"));
        }
    }

    public void resize(int globalRegisters) {
        int globalCount = globalRegisters;
        int localCount = registerPool.length - globalRegisters;
        if (globalCount < 0 || localCount < 2) {
            System.err.println("bad global register count " + globalCount);
            globalCount = registerPool.length / 2;
            localCount = registerPool.length - globalRegisters;
        }
        localRegisterPool = new int[localCount];
        globalRegisterPool = new int[globalCount];
        for (int i = 0; i < registerPool.length; i++) {
            if (i < localCount) {
                localRegisterPool[i] = registerPool[i];
            }
            else {
                globalRegisterPool[i - localCount] = registerPool[i];
            }
        }
        writeBackBuffer = new boolean[localRegisterPool.length];
        abstractVariableBuffer = new AbstractLeftValue[localRegisterPool.length];
    }

    public boolean processCheckValid(AbstractLeftValue variable) {
        for (AbstractLeftValue i : abstractVariableBuffer) {
            if (i == variable) {
                return true;
            }
        }
        return false;
    }

    public void processMarkRegister(AbstractVariable abstractVariable) {
        for (int i = 0; i < localRegisterPool.length; i++) {
            if (abstractVariableBuffer[i] != null && abstractVariableBuffer[i].equals(abstractVariable)) {
                writeBackBuffer[i] = true;
            }
        }
    }

    public Pair<Boolean, Integer> processReplaceRegister(AbstractLeftValue abstractLeftValue) {
        for (int i = 0; i < localRegisterPool.length; i++) {
            if (abstractVariableBuffer[i] != null && abstractVariableBuffer[i].equals(abstractLeftValue)) {
                return new Pair<>(false, localRegisterPool[i]);
            }
        }
        for (int i = 0; i < localRegisterPool.length; i++) {
            if (abstractVariableBuffer[i] == null) {
                abstractVariableBuffer[i] = abstractLeftValue;
                return new Pair<>(true, localRegisterPool[i]);
            }
        }
        int index = -1;
        int maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < localRegisterPool.length; i++) {
            assert abstractVariableBuffer[i] != null;
            if (abstractVariableBuffer[i].getBasicBlock() == abstractLeftValue.getBasicBlock()
                    && abstractVariableBuffer[i].getIndex() == abstractLeftValue.getIndex()) {
                continue;
            }
            int next = abstractVariableBuffer[i].getBasicBlock().nextIndexOf(
                    abstractVariableBuffer[i], abstractVariableBuffer[i].getIndex());
            if (next > maxValue) {
                maxValue = next;
                index = i;
            }
        }
        if (Settings.optTempRegisterOptimize && abstractVariableBuffer[index] != null
                && writeBackBuffer[index]) {
            abstractVariableBuffer[index].writeBack(this);
            writeBackBuffer[index] = false;
        }
        abstractVariableBuffer[index] = abstractLeftValue;
        return new Pair<>(true, localRegisterPool[index]);
    }

    public void processRemoveRegister(AbstractVariable abstractVariable) {
        for (int i = 0; i < localRegisterPool.length; i++) {
            if (abstractVariableBuffer[i] != null && abstractVariableBuffer[i].equals(abstractVariable)) {
                abstractVariableBuffer[i] = null;
                writeBackBuffer[i] = false;
            }
        }
    }

    public void processClearLocalRegister() {
        Arrays.fill(abstractVariableBuffer, null);
        Arrays.fill(writeBackBuffer, false);
    }

    public void processFlushLocalRegister(HashSet<AbstractLeftValue> onlyInSet) {
        for (int i = 0; i < localRegisterPool.length; i++) {
            if (!writeBackBuffer[i]) {
                continue;
            }
            if (onlyInSet.contains(abstractVariableBuffer[i])) {
                abstractVariableBuffer[i].writeBack(this);
            }
            else if (abstractVariableBuffer[i] instanceof GlobalVariable) {
                abstractVariableBuffer[i].writeBack(this);
            }
        }
    }

    public void processFlushLocalRegister() {
        for (int i = 0; i < localRegisterPool.length; i++) {
            if (!writeBackBuffer[i]) {
                continue;
            }
            if (abstractVariableBuffer[i] instanceof GlobalVariable) {
                abstractVariableBuffer[i].writeBack(this);
            }
        }
    }

    // --------------------------------------------------------------------------------

    public void processStringConst(HashMap<Integer, String> addrToString) {
        addrToString.forEach((i, j) -> initString.add("str_" + i + ": .asciiz \"" + j + "\""));
    }

    public void processComment(String comment, int level) {
        AbstractInstruction instruction = AbstractInstruction.getComment(comment);
        instruction.setIndent(level);
        objectCode.add(instruction);
    }

    public void processGlobalSpace(int globalSpace) {
        this.globalSpace = globalSpace;
        if (globalSpace != 0) {
            AbstractInstruction instruction = AbstractInstruction.getLoadTag(MipsAssembly.gp, "global");
            instruction.setIndent(1);
            objectCode.add(instruction);
        }
    }

    public void processTag(AbstractOperand operand) {
        AbstractInstruction instruction = AbstractInstruction.getTag(operand.operand());
        if (operand instanceof Function) {
            currentFunction = (Function)operand;
            instruction.setIndent(2);
            objectCode.add(instruction);
            for (int i = 0; i < currentFunction.getParams(); i++) {
                int register = currentFunction.getParam(i).getRegister();
                if (register > 0) {
                    objectCode.add(AbstractInstruction.getLoad(register
                            , (i + 1) << 2, MipsAssembly.sp));
                }
            }
        }
        else {
            objectCode.add(instruction);
        }
    }

    public void processJump(TagString tag) {
        objectCode.add(AbstractInstruction.getJump(tag.operand()));
    }

    // --------------------------------------------------------------------------------

    public void processMove(int dstRegister, int srcRegister) {
        MipsCalTemplate.moveTemplate(dstRegister, srcRegister, objectCode);
    }

    public void processLoadRegister(int dstRegister, int offset, int base) {
        MipsMemTemplate.loadRegisterTemplate(dstRegister, offset, base, objectCode);
    }

    public void processStoreRegister(int dstRegister, int offset, int base) {
        MipsMemTemplate.storeRegisterTemplate(dstRegister, offset, base, objectCode);
    }

    public void processAllocMainStack(int space) {
        MipsCalTemplate.addValueTemplate(MipsAssembly.sp, MipsAssembly.sp, -space << 2, objectCode);
    }

    public void processBranchNotZero(AbstractVariable src, TagString thenTag, TagString elseTag) {
        MipsLogicTemplate.branchNotZeroTemplate(src,
                thenTag.operand(), elseTag.operand(), objectCode, this);
    }

    public void processBranchNotEqual(AbstractVariable src1, AbstractVariable src2
            , TagString thenTag, TagString elseTag) {
        MipsLogicTemplate.branchNotEqualTemplate(src1, src2,
                thenTag.operand(), elseTag.operand(), objectCode, this);
    }

    public void processBranchIfEqual(AbstractVariable src1, AbstractVariable src2
            , TagString thenTag, TagString elseTag) {
        MipsLogicTemplate.branchIfEqualTemplate(src1, src2,
                thenTag.operand(), elseTag.operand(), objectCode, this);
    }

    public void processBranchGreaterOrEqualZero(AbstractVariable src, TagString thenTag, TagString elseTag) {
        MipsLogicTemplate.branchGreaterOrEqualZeroTemplate(src,
                thenTag.operand(), elseTag.operand(), objectCode, this);
    }

    public void processBranchGreaterThanZero(AbstractVariable src, TagString thenTag, TagString elseTag) {
        MipsLogicTemplate.branchGreaterThanTemplate(src,
                thenTag.operand(), elseTag.operand(), objectCode, this);
    }

    public void processBranchLessOrEqualZero(AbstractVariable src, TagString thenTag, TagString elseTag) {
        MipsLogicTemplate.branchLessOrEqualZeroTemplate(src,
                thenTag.operand(), elseTag.operand(), objectCode, this);
    }

    public void processBranchLessThanZero(AbstractVariable src, TagString thenTag, TagString elseTag) {
        MipsLogicTemplate.branchLessThanZeroTemplate(src,
                thenTag.operand(), elseTag.operand(), objectCode, this);
    }

    public void processAdd(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsCalTemplate.addTemplate(dst, src1, src2, objectCode, this);
    }

    public void processSub(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsCalTemplate.subTemplate(dst, src1, src2, objectCode, this);
    }

    public void processMul(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsCalTemplate.mulTemplate(dst, src1, src2, objectCode, this);
    }

    public void processDiv(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsCalTemplate.divTemplate(dst, src1, src2, objectCode, this);
    }

    public void processMod(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsCalTemplate.modTemplate(dst, src1, src2, objectCode, this);
    }

    public void processSll(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsCalTemplate.sllTemplate(dst, src1, src2, objectCode, this);
    }

    public void processSetEqual(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsLogicTemplate.seqTemplate(dst, src1, src2, objectCode, this);
    }

    public void processSetNotEqual(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsLogicTemplate.sneTemplate(dst, src1, src2, objectCode, this);
    }

    public void processSetGreatEqual(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsLogicTemplate.sleTemplate(dst, src2, src1, objectCode, this);
    }

    public void processSetGreatThan(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsLogicTemplate.sltTemplate(dst, src2, src1, objectCode, this);
    }

    public void processLessEqual(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsLogicTemplate.sleTemplate(dst, src1, src2, objectCode, this);
    }

    public void processLessThan(AbstractLeftValue dst, AbstractVariable src1, AbstractVariable src2) {
        MipsLogicTemplate.sltTemplate(dst, src1, src2, objectCode, this);
    }

    public void processLoadWord(AbstractLeftValue dst, AbstractVariable addr) {
        MipsMemTemplate.loadWordTemplate(dst, addr, objectCode, this);
    }

    public void processStoreWord(AbstractVariable src, AbstractVariable addr) {
        MipsMemTemplate.storeWordTemplate(src, addr, objectCode, this);
    }

    public void processLoadAddress(AbstractLeftValue dst, int offset) {
        MipsMemTemplate.loadAddrTemplate(dst, offset, objectCode, this);
    }

    public void processScan(AbstractLeftValue dst) {
        MipsFuncTemplate.scanTemplate(dst, objectCode, this);
    }

    public void processPutString(ConstValue address) {
        MipsFuncTemplate.printStrTemplate(address, objectCode);
    }

    public void processPutNumber(AbstractVariable abstractVariable) {
        MipsFuncTemplate.printNumTemplate(abstractVariable, objectCode, this);
    }

    public void processParam(AbstractVariable abstractVariable) {
        MipsFuncTemplate.funcParamTemplate(abstractVariable, funcParams);
    }

    public void processCallFunc(Function function) {
        MipsFuncTemplate.funcCallTemplate(function, funcParams, objectCode, this);
    }

    public void processBackFunc(AbstractVariable abstractVariable) {
        MipsFuncTemplate.funcReturnTemplate(currentFunction, abstractVariable, objectCode, this);
    }

    public void optimize() {
        while (true) {
            boolean optimized = MipsCodeOptimizer.optimizeJumpTag(objectCode);
            optimized |= MipsCodeOptimizer.optimizeLoadStore(objectCode);
            if (!optimized) {
                break;
            }
        }
    }

    public void dump() {
        (new MipsCodeDumper(globalSpace, initString, objectCode, dataField)).dump();
    }
}