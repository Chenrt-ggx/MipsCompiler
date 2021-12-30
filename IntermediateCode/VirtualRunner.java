package IntermediateCode;

import Global.Settings;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.ParamVariable;
import IntermediateCode.Operands.GlobalVariable;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public class VirtualRunner {
    private static class Mode {
        private int allocIndex;
        private int currentIndex;

        private boolean ended;
        private boolean branched;
        private TagString basicBlock;

        private final Scanner cin;
        private final int maxSteps;

        private Mode(int maxSteps) {
            basicBlock = null;
            this.maxSteps = maxSteps;
            ended = branched = false;
            cin = new Scanner(System.in);
            allocIndex = currentIndex = 0;
        }

        private String basicBlockName() {
            if (basicBlock == null) {
                return "start_block";
            }
            return basicBlock.toString();
        }
    }

    private static class StackFrame {
        private final Function function;
        private final Integer returnIndex;
        private final Integer popAllocIndex;
        private final TagString returnBasicBlock;
        private final HashMap<AbstractVariable, Integer> variableMap;

        private StackFrame(Function function, int returnIndex
                , int popAllocIndex, TagString returnBasicBlock) {
            this.function = function;
            this.returnIndex = returnIndex;
            this.variableMap = new HashMap<>();
            this.popAllocIndex = popAllocIndex;
            this.returnBasicBlock = returnBasicBlock;
        }

        private String functionName() {
            if (function == null) {
                return "pre_main";
            }
            return function.toString();
        }
    }

    private final Mode mode;
    private final StringBuilder outBuffer;
    private final IntermediateCode intermediate;

    private final ArrayList<Integer> memory;
    private final LinkedList<StackFrame> stack;
    private final HashMap<Integer, String> stringPool;
    private final HashMap<ParamVariable, Integer> funcParams;

    public VirtualRunner(IntermediateCode intermediateCode, int maxSteps) {
        mode = new Mode(maxSteps);
        this.intermediate = intermediateCode;
        outBuffer = new StringBuilder();
        stack = new LinkedList<>();
        memory = new ArrayList<>();
        funcParams = new HashMap<>();
        stringPool = new HashMap<>();
        stringPool.putAll(intermediate.getStringPool());
        stack.add(new StackFrame(null, -1, 0, null));
    }

    public void run() {
        int steps = 0;
        while (steps < mode.maxSteps && !mode.ended) {
            intermediate.executeIndex(stack.getLast().function,
                    mode.basicBlock, mode.currentIndex, this);
            mode.currentIndex += mode.branched ? 0 : 1;
            mode.branched = false;
            steps++;
        }
        if (steps >= mode.maxSteps) {
            System.err.println("step limit exceed at virtual runner");
            (new Exception()).printStackTrace();
        }
        if (Settings.codeSimulateTraceToStdout) {
            System.out.println();
        }
        System.out.print(outBuffer);
        System.out.println();
    }

    public ArrayList<Integer> getDataField() {
        int steps = 0;
        while (steps < mode.maxSteps && stack.getLast().function == null) {
            intermediate.executeIndex(stack.getLast().function,
                    mode.basicBlock, mode.currentIndex, this);
            mode.currentIndex += mode.branched ? 0 : 1;
            mode.branched = false;
            steps++;
        }
        if (steps >= mode.maxSteps) {
            System.err.println("step limit exceed when getting data field");
            (new Exception()).printStackTrace();
        }
        int endIndex = -1;
        for (int i = memory.size() - 1; i >= 0; i--) {
            if (memory.get(i) != 0) {
                endIndex = i + 1;
                break;
            }
        }
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < endIndex; i++) {
            result.add(memory.get(i));
        }
        return result;
    }

    public int getVariable(AbstractVariable id) {
        if (stack.getLast().variableMap.containsKey(id)) {
            return stack.getLast().variableMap.get(id);
        }
        if (stack.getFirst().variableMap.containsKey(id)) {
            return stack.getFirst().variableMap.get(id);
        }
        if (id instanceof ConstValue) {
            return ((ConstValue)id).getValue();
        }
        System.err.println("unknown variable " + id
                + " occurred when getting variable at "
                + "@function " + stack.getLast().functionName() + ", "
                + "#basic_block " + mode.basicBlockName() + ", "
                + "line " + (mode.currentIndex + 1));
        (new Exception()).printStackTrace();
        return -1;
    }

    public String tryGetVariable(AbstractVariable id) {
        if (stack.getLast().variableMap.containsKey(id)) {
            return "" + stack.getLast().variableMap.get(id);
        }
        if (stack.getFirst().variableMap.containsKey(id)) {
            return "" + stack.getFirst().variableMap.get(id);
        }
        if (id instanceof ConstValue) {
            return "" + ((ConstValue)id).getValue();
        }
        return "invalid";
    }

    public void setVariable(AbstractVariable id, int val) {
        if (id instanceof GlobalVariable) {
            if (!stack.getFirst().variableMap.containsKey(id)) {
                if (stack.getLast().function != null) {
                    System.err.println("unexpected global variable " + id
                            + " declared at @function " + stack.getLast().functionName() + ", "
                            + "#basic_block " + mode.basicBlockName() + ", "
                            + "line " + (mode.currentIndex + 1));
                    (new Exception()).printStackTrace();
                }
                else {
                    mode.allocIndex++;
                    memory.add(val);
                }
            }
            stack.getFirst().variableMap.put(id, val);
        }
        else if (id instanceof AbstractLeftValue) {
            stack.getLast().variableMap.put(id, val);
        }
        else {
            System.err.println("uneditable variable " + id
                    + " occurred when setting variable at "
                    + "@function " + stack.getLast().functionName() + ", "
                    + "#basic_block " + mode.basicBlockName() + ", "
                    + "line " + (mode.currentIndex + 1));
            (new Exception()).printStackTrace();
        }
    }

    public int getMemory(int addr) {
        if (addr < 0 || addr >> 2 >= mode.allocIndex) {
            System.err.println("addr " + (addr >> 2) + " out of range "
                    + mode.allocIndex + " when getting memory at "
                    + "@function " + stack.getLast().functionName() + ", "
                    + "#basic_block " + mode.basicBlockName() + ", "
                    + "line " + (mode.currentIndex + 1));
            (new Exception()).printStackTrace();
            return Integer.MIN_VALUE;
        }
        else {
            return memory.get(addr >> 2);
        }
    }

    public String tryGetMemory(String addr) {
        try {
            int realAddr = Integer.parseInt(addr);
            if (realAddr < 0 || realAddr >> 2 >= mode.allocIndex) {
                return "invalid";
            }
            else {
                return "" + memory.get(realAddr >> 2);
            }
        }
        catch (NumberFormatException e) {
            return "invalid";
        }
    }

    public void setMemory(int addr, int val) {
        if (addr < 0 || addr >> 2 >= mode.allocIndex) {
            System.err.println("addr " + (addr >> 2) + " out of range "
                    + mode.allocIndex + " when setting memory at "
                    + "@function " + stack.getLast().functionName() + ", "
                    + "#basic_block " + mode.basicBlockName() + ", "
                    + "line " + (mode.currentIndex + 1));
            (new Exception()).printStackTrace();
        }
        else {
            memory.set(addr >> 2, val);
        }
    }

    public int allocMemory(int size) {
        for (int i = 0; i < size; i++) {
            if (i + mode.allocIndex >= memory.size()) {
                memory.add(0);
            }
            else {
                memory.set(i + mode.allocIndex, 0);
            }
        }
        int ret = mode.allocIndex;
        mode.allocIndex += size;
        return ret << 2;
    }

    public String getString(int addr) {
        if (stringPool.containsKey(addr)) {
            return stringPool.get(addr);
        }
        System.err.println("unknown addr occurred when getting string at "
                + "@function " + stack.getLast().functionName() + ", "
                + "#basic_block " + mode.basicBlockName() + ", "
                + "line " + (mode.currentIndex + 1));
        (new Exception()).printStackTrace();
        return null;
    }

    public void processBranch(TagString tag) {
        mode.branched = true;
        mode.basicBlock = tag;
        mode.currentIndex = 0;
    }

    public void pushParam(AbstractVariable id) {
        ParamVariable trans = new ParamVariable(funcParams.size());
        funcParams.put(trans, getVariable(id));
    }

    public void callFunc(Function func) {
        mode.branched = true;
        stack.add(new StackFrame(func, mode.currentIndex + 1,
                mode.allocIndex, mode.basicBlock));
        stack.getLast().variableMap.putAll(funcParams);
        mode.currentIndex = 0;
        mode.basicBlock = null;
        funcParams.clear();
    }

    public void exitFunc(int ret, boolean hasReturn) {
        mode.branched = true;
        mode.currentIndex = stack.getLast().returnIndex;
        mode.allocIndex = stack.getLast().popAllocIndex;
        mode.basicBlock = stack.getLast().returnBasicBlock;
        stack.removeLast();
        if (hasReturn) {
            stack.getLast().variableMap.put(intermediate.getReturnValue(), ret);
        }
        if (stack.size() <= 1) {
            mode.ended = true;
        }
    }

    public int readInt() {
        return mode.cin.nextInt();
    }

    public void printString(String toPrint) {
        outBuffer.append(toPrint.replaceAll("\\\\n", "\n"));
    }
}