package IntermediateCode;

import java.util.Map;
import Global.Settings;
import Global.LogBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.ReturnValue;

public class IntermediateCode {
    private final HashMap<Integer, String> addrToString;
    private final HashMap<ConstValue, Integer> constToValue;

    private final Function mainFunction;
    private final BasicBlock entranceBlock;
    private final ReturnValue returnValue;
    private final LinkedHashMap<Function, FunctionBlock> functionMap;

    public IntermediateCode(HashMap<ConstValue, Integer> constToValue
            , HashMap<Integer, String> addrToString, ReturnValue returnValue
            , Function mainFunction, BasicBlock entranceBlock
            , LinkedHashMap<Function, FunctionBlock> functionMap) {
        this.returnValue = returnValue;
        this.functionMap = functionMap;
        this.addrToString = addrToString;
        this.mainFunction = mainFunction;
        this.constToValue = constToValue;
        this.entranceBlock = entranceBlock;
    }

    public void optimize() {
        entranceBlock.branchInstructionOptimize();
        functionMap.forEach((k, v) -> v.optimize());
    }

    protected ReturnValue getReturnValue() {
        return returnValue;
    }

    protected HashMap<Integer, String> getStringPool() {
        return addrToString;
    }

    protected void executeIndex(Function function, TagString tagString, int index, VirtualRunner runner) {
        if (function != null) {
            functionMap.get(function).executeIndex(tagString, index, runner);
            return;
        }
        if (index < entranceBlock.getInstrCount()) {
            entranceBlock.executeIndex(index, null, runner);
            return;
        }
        runner.callFunc(mainFunction);
    }

    public void toAssembly(MipsAssembly mipsAssembly) {
        mipsAssembly.processGlobalSpace(entranceBlock.pushUpSpace(0));
        functionMap.forEach((k, v) -> v.pushUpSpace());
        mipsAssembly.processStringConst(addrToString);
        mipsAssembly.processComment("@init pre_main:", 1);
        entranceBlock.toAssembly(null, mipsAssembly);
        mipsAssembly.processAllocMainStack(mainFunction.getSpace());
        functionMap.get(mainFunction).toAssembly(mipsAssembly);
        functionMap.forEach((k, v) -> {
            if (!mainFunction.equals(k)) {
                v.toAssembly(mipsAssembly);
            }
        });
        if (Settings.processToStdout) {
            System.out.println();
        }
    }

    private void dumpItem(String str) {
        if (Settings.intermediateCodeToFile) {
            LogBuffer.buffer.add(str);
        }
        if (Settings.intermediateCodeToStdout) {
            System.out.println(str);
        }
    }

    public void dump() {
        constToValue.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(i ->
                dumpItem(i.getKey() + " -> " + i.getValue()));
        dumpItem("");
        addrToString.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(i ->
                dumpItem("addr_" + i.getKey() + " -> \"" + i.getValue() + "\""));
        dumpItem("");
        dumpItem("@init pre_main:");
        entranceBlock.dump();
        dumpItem("");
        functionMap.get(mainFunction).dump();
        dumpItem("");
        functionMap.forEach((k, v) -> {
            if (!mainFunction.equals(k)) {
                v.dump();
                dumpItem("");
            }
        });
    }
}