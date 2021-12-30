package IntermediateCode;

import Global.Pair;
import Global.Settings;
import Global.LogBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import IntermediateCode.InterElement.BranchElement;
import IntermediateCode.InterElement.AbstractElement;

public class FunctionBlock {
    private int space;
    private BasicBlock startBlock;
    private final Function function;
    private final HashSet<BasicBlock> endBlockSet;
    private final ArrayList<BasicBlock> basicBlocks;
    private final HashMap<TagString, BasicBlock> nameToBasicBlock;
    private final ArrayList<Pair<AbstractElement, Integer>> intermediates;

    protected FunctionBlock(Function function) {
        space = 0;
        startBlock = null;
        this.function = function;
        endBlockSet = new HashSet<>();
        basicBlocks = new ArrayList<>();
        intermediates = new ArrayList<>();
        nameToBasicBlock = new HashMap<>();
    }

    protected int size() {
        return intermediates.size();
    }

    protected BasicBlock getBasicBlockById(int id) {
        return basicBlocks.get(id);
    }

    protected void addIntermediate(AbstractElement element, int level) {
        intermediates.add(new Pair<>(element, level));
    }

    private BasicBlock putNewBlock(BasicBlock currentBlock) {
        if (currentBlock.isEndBlock()) {
            endBlockSet.add(currentBlock);
        }
        currentBlock.getBlockTags().forEach(i -> nameToBasicBlock.put(i, currentBlock));
        basicBlocks.add(currentBlock);
        return new BasicBlock(basicBlocks.size(), this);
    }

    private void initBasicBlock(HashSet<TagString> usedTagSet) {
        BasicBlock currentBlock = startBlock;
        for (Pair<AbstractElement, Integer> i : intermediates) {
            if (i.getKey() instanceof BranchElement || i.getKey().getName().equals("jmp")) {
                currentBlock.addIntermediate(i);
                currentBlock = putNewBlock(currentBlock);
                continue;
            }
            if (i.getKey().getName().equals("tag")) {
                TagString tagString = (TagString)i.getKey().getMain();
                if (!usedTagSet.contains(tagString)) {
                    continue;
                }
                if (currentBlock.isPureTag()) {
                    currentBlock.addIntermediate(i);
                    continue;
                }
                currentBlock = putNewBlock(currentBlock);
                currentBlock.addIntermediate(i);
                continue;
            }
            currentBlock.addIntermediate(i);
        }
        putNewBlock(currentBlock);
    }

    protected void finalizeFunction() {
        HashSet<TagString> usedTagSet = new HashSet<>();
        intermediates.forEach(i -> {
            if (i.getKey() instanceof BranchElement) {
                Collections.addAll(usedTagSet, ((BranchElement)i.getKey()).getTags());
            }
            else if (i.getKey().getName().equals("jmp")) {
                usedTagSet.add((TagString)i.getKey().getMain());
            }
        });
        startBlock = new BasicBlock(basicBlocks.size(), this);
        initBasicBlock(usedTagSet);
        basicBlocks.forEach(i -> i.nextTag().forEach(j -> {
            i.addSuccessor(nameToBasicBlock.get(j));
            nameToBasicBlock.get(j).addPredecessor(i);
        }));
    }

    private void arriveDefinitionAnalyse() {
        basicBlocks.forEach(BasicBlock::pushUpGen);
        startBlock.pushUpParams(function);
        basicBlocks.forEach(i -> basicBlocks.stream().filter(j -> i != j)
                .forEach(j -> j.pushUpKill(i.getGenSet())));
        HashSet<BasicBlock> unique = new HashSet<>();
        ArrayList<BasicBlock> order = new ArrayList<>();
        startBlock.searchSuccessor(unique, order);
        if (order.size() != basicBlocks.size()) {
            System.err.println("bad stream graph occurred at @function "
                    + function + " at arrive definition analyse");
        }
        for (int i = order.stream().mapToInt(j -> j.pushUpArriveInOut() ? 1 : 0)
                .reduce(0, (x, y) -> x | y); i > 0; ) {
            i = order.stream().mapToInt(j -> j.pushUpArriveInOut() ? 1 : 0)
                    .reduce(0, (x, y) -> x | y);
        }
    }

    private void activeVariableAnalyse() {
        basicBlocks.forEach(BasicBlock::pushUpDefUse);
        HashSet<BasicBlock> unique = new HashSet<>();
        ArrayList<BasicBlock> order = new ArrayList<>();
        endBlockSet.forEach(i -> i.searchPredecessor(unique, order));
        if (order.size() != basicBlocks.size()) {
            System.err.println("bad stream graph occurred at @function "
                    + function + " at active variable analyse");
        }
        for (int i = order.stream().mapToInt(j -> j.pushUpActiveInOut() ? 1 : 0)
                .reduce(0, (x, y) -> x | y); i > 0; ) {
            i = order.stream().mapToInt(j -> j.pushUpActiveInOut() ? 1 : 0)
                    .reduce(0, (x, y) -> x | y);
        }
    }

    protected void optimize() {
        while (true) {
            activeVariableAnalyse();
            int updated = basicBlocks.stream().mapToInt(j -> j.deadCodeElimination() ? 1 : 0)
                    .reduce(0, (x, y) -> x | y);
            arriveDefinitionAnalyse();
            HashMap<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>
                    , AbstractVariable> defineMap = new HashMap<>();
            basicBlocks.forEach(i -> i.updateSpreadAble(defineMap));
            updated += basicBlocks.stream().mapToInt(j -> j.variableSpread(defineMap) ? 1 : 0)
                    .reduce(0, (x, y) -> x | y);
            if (updated == 0) {
                break;
            }
        }
        basicBlocks.forEach(BasicBlock::branchInstructionOptimize);
    }

    protected void executeIndex(TagString tagString, int index, VirtualRunner runner) {
        if (tagString == null) {
            startBlock.executeIndex(index, function, runner);
            return;
        }
        BasicBlock executeBlock = nameToBasicBlock.get(tagString);
        if (index < executeBlock.getInstrCount()) {
            executeBlock.executeIndex(index, function, runner);
            return;
        }
        runner.processBranch(basicBlocks.get(executeBlock
                .getBlockId() + 1).getBlockTags().iterator().next());
    }

    protected void pushUpSpace() {
        space = function.getParams() + 1;
        basicBlocks.forEach(i -> space = i.pushUpSpace(space));
        function.setSpace(space);
    }

    protected void toAssembly(MipsAssembly mipsAssembly) {
        if (Settings.graphColoringRegisterAllocation && Settings.optTempRegisterOptimize) {
            if (basicBlocks.stream().anyMatch(i -> i.size() > (1 << 9))) {
                Settings.colorIncludeParamRegister = false;
                mipsAssembly.resize(6);
            }
            else {
                Settings.colorIncludeParamRegister = true;
                mipsAssembly.resize(18);
            }
        }
        else if (Settings.graphColoringRegisterAllocation) {
            Settings.colorIncludeParamRegister = true;
            mipsAssembly.resize(22);
        }
        else {
            Settings.colorIncludeParamRegister = false;
            mipsAssembly.resize(0);
        }
        if (Settings.graphColoringRegisterAllocation) {
            basicBlocks.forEach(i -> i.countVariableUsage());
            activeVariableAnalyse();
            ConflictGraph conflictGraph = new ConflictGraph();
            basicBlocks.forEach(i -> i.buildConflictGraph(conflictGraph));
            conflictGraph.colorNode();
        }
        basicBlocks.forEach(i -> i.pushUpFunctionCall());
        mipsAssembly.processComment("@function " + function + ":", 1);
        AbstractElement.getTagElement(function).toMipsAssembly(mipsAssembly);
        basicBlocks.forEach(i -> i.toAssembly(function, mipsAssembly));
    }

    private void dumpItem(String str) {
        if (Settings.intermediateCodeToFile) {
            LogBuffer.buffer.add(str);
        }
        if (Settings.intermediateCodeToStdout) {
            System.out.println(str);
        }
    }

    protected void dump() {
        dumpItem("@function " + function + ":");
        basicBlocks.forEach(BasicBlock::dump);
    }
}