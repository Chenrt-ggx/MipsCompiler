package IntermediateCode;

import Global.Pair;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.ReturnValue;
import IntermediateCode.Operands.ParamVariable;
import IntermediateCode.Operands.GlobalVariable;
import IntermediateCode.Operands.NormalVariable;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.InterElement.AbstractElement;

public class IntermediateBuilder {
    private int tempCount;
    private int globalCount;
    private int variableCount;
    private boolean functionUpdated;
    private Function currentFunction;
    private FunctionBlock currentFunctionBlock;

    private final HashMap<String, Integer> tagCount;
    private final HashMap<Integer, String> addrToString;
    private final HashMap<String, Integer> stringToAddr;
    private final HashMap<Integer, ConstValue> valueToConst;
    private final HashMap<ConstValue, Integer> constToValue;

    private final ReturnValue returnValue;
    private final BasicBlock entranceBlock;
    private final LinkedHashMap<Function, FunctionBlock> functionMap;

    public IntermediateBuilder() {
        currentFunction = null;
        functionUpdated = false;
        tagCount = new HashMap<>();
        currentFunctionBlock = null;
        constToValue = new HashMap<>();
        valueToConst = new HashMap<>();
        addrToString = new HashMap<>();
        stringToAddr = new HashMap<>();
        returnValue = new ReturnValue();
        functionMap = new LinkedHashMap<>();
        tempCount = globalCount = variableCount = 0;
        entranceBlock = new BasicBlock(-1, null);
    }

    public void putIntermediate(AbstractElement element, int level) {
        if (element.getName().equals("tag") && element.getMain() instanceof Function) {
            currentFunction = (Function)element.getMain();
            currentFunctionBlock = new FunctionBlock(currentFunction);
        }
        else if (currentFunctionBlock == null) {
            entranceBlock.addIntermediate(new Pair<>(element, 0));
        }
        else {
            currentFunctionBlock.addIntermediate(element, level);
            functionMap.put(currentFunction, currentFunctionBlock);
        }
    }

    public int putStringConst(String str) {
        if (stringToAddr.containsKey(str)) {
            return stringToAddr.get(str);
        }
        int id = stringToAddr.size();
        addrToString.put(id, str);
        stringToAddr.put(str, id);
        return id;
    }

    public AbstractVariable putIntConst(int value) {
        if (valueToConst.containsKey(value)) {
            return valueToConst.get(value);
        }
        ConstValue constValue = new ConstValue(value);
        constToValue.put(constValue, value);
        valueToConst.put(value, constValue);
        return constValue;
    }

    public TagString putTag(String prefix) {
        tagCount.compute(prefix, (k, v) -> v == null ? 0 : v + 1);
        String tagString = prefix.toUpperCase() + "_" + tagCount.get(prefix);
        return new TagString(tagString);
    }

    public Function putFunction(String name, ArrayList<ParamVariable> params, boolean isMain) {
        return new Function("func_" + name, params, isMain);
    }

    public AbstractVariable putParamDef(int index) {
        return new ParamVariable(index);
    }

    public AbstractVariable putVariable(boolean isDeclared) {
        return currentFunction == null ?
                new GlobalVariable(globalCount++) : isDeclared ?
                new NormalVariable(variableCount++, false) :
                new NormalVariable(tempCount++, true);
    }

    public AbstractVariable putVariable() {
        return putVariable(false);
    }

    public AbstractVariable putReturnValue() {
        return returnValue;
    }

    public int getCurrentFunctionSize() {
        if (currentFunctionBlock == null) {
            return entranceBlock.size();
        }
        else {
            return currentFunctionBlock.size();
        }
    }

    public IntermediateCode toIntermediateCode() {
        if (!functionUpdated) {
            functionUpdated = true;
            functionMap.forEach((k, v) -> v.finalizeFunction());
        }
        if (!currentFunction.isMain()) {
            System.err.println("bad main function " + currentFunction
                    + " occurred when building intermediate code");
        }
        return new IntermediateCode(constToValue, addrToString, returnValue,
                currentFunction, entranceBlock, functionMap);
    }
}