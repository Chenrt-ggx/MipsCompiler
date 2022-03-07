package IntermediateCode;

import Global.Pair;
import Global.RedBlackTree;
import Global.Settings;
import Global.LogBuffer;

import java.util.*;
import java.util.stream.Collectors;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.Operands.Function;
import IntermediateCode.Operands.TagString;
import IntermediateCode.Operands.GlobalVariable;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import IntermediateCode.InterElement.BranchElement;
import IntermediateCode.InterElement.AbstractElement;
import IntermediateCode.InterElement.CalculateElement;

public class BasicBlock {
    private boolean isEnd;
    private final int blockId;
    private final FunctionBlock functionBlock;
    private final HashSet<TagString> blockTags;
    private final HashSet<BasicBlock> successor;
    private final HashSet<BasicBlock> predecessor;
    private ArrayList<AbstractElement> intermediates;
    private final HashMap<AbstractLeftValue, RedBlackTree<Integer>> variablePlace;

    private final HashSet<AbstractLeftValue> defSet;
    private final HashSet<AbstractLeftValue> useSet;
    private final HashSet<AbstractLeftValue> activeInSet;
    private final HashSet<AbstractLeftValue> activeOutSet;
    private final HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> genSet;
    private final HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> killSet;
    private final HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> arriveInSet;
    private final HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> arriveOutSet;

    protected BasicBlock(int blockId, FunctionBlock functionBlock) {
        this.isEnd = false;
        this.blockId = blockId;
        defSet = new HashSet<>();
        useSet = new HashSet<>();
        genSet = new HashSet<>();
        killSet = new HashSet<>();
        blockTags = new HashSet<>();
        successor = new HashSet<>();
        predecessor = new HashSet<>();
        activeInSet = new HashSet<>();
        arriveInSet = new HashSet<>();
        activeOutSet = new HashSet<>();
        arriveOutSet = new HashSet<>();
        intermediates = new ArrayList<>();
        variablePlace = new HashMap<>();
        this.functionBlock = functionBlock;
    }

    public HashSet<AbstractLeftValue> getActiveOutSet() {
        return activeOutSet;
    }

    public int nextIndexOf(AbstractLeftValue abstractLeftValue, int index) {
        RedBlackTree<Integer> placeSet = variablePlace.get(abstractLeftValue);
        try {
            return placeSet.upperBound(index);
        }
        catch (NoSuchElementException e) {
            return Integer.MAX_VALUE;
        }
    }

    protected int size() {
        return intermediates.size();
    }

    protected int getBlockId() {
        return blockId;
    }

    protected boolean isEndBlock() {
        return isEnd;
    }

    protected boolean isPureTag() {
        return intermediates.isEmpty();
    }

    protected HashSet<TagString> getBlockTags() {
        return blockTags;
    }

    protected int getInstrCount() {
        return intermediates.size();
    }

    protected void addPredecessor(BasicBlock block) {
        predecessor.add(block);
    }

    protected void addSuccessor(BasicBlock block) {
        successor.add(block);
    }

    protected void addIntermediate(Pair<AbstractElement, Integer> element) {
        if (element.getKey().getName().equals("tag")) {
            if (isPureTag()) {
                blockTags.add((TagString)element.getKey().getMain());
            }
            else {
                System.err.println("unexpected tag string " + element.getKey().getMain()
                        + " occurred in #basic_block " + blockId);
            }
        }
        else if (!isEnd) {
            element.getKey().setLoopLevel(element.getValue());
            intermediates.add(element.getKey());
            isEnd = element.getKey().getName().equals("exit");
        }
    }

    protected LinkedList<TagString> nextTag() {
        LinkedList<TagString> tagList = new LinkedList<>();
        AbstractElement element = intermediates.get(intermediates.size() - 1);
        if (!isEnd && element instanceof BranchElement) {
            Collections.addAll(tagList, ((BranchElement)element).getTags());
        }
        else if (!isEnd && element.getName().equals("jmp")) {
            tagList.add((TagString)element.getMain());
        }
        else if (!isEnd && functionBlock != null) {
            tagList.add(functionBlock.getBasicBlockById(blockId + 1).blockTags.iterator().next());
        }
        return tagList;
    }

    protected void searchPredecessor(HashSet<BasicBlock> unique, ArrayList<BasicBlock> order) {
        unique.add(this);
        order.add(this);
        predecessor.stream().filter(i -> !unique.contains(i))
                .forEach(i -> i.searchPredecessor(unique, order));
    }

    protected void searchSuccessor(HashSet<BasicBlock> unique, ArrayList<BasicBlock> order) {
        unique.add(this);
        order.add(this);
        successor.stream().filter(i -> !unique.contains(i))
                .forEach(i -> i.searchSuccessor(unique, order));
    }

    protected void pushUpDefUse() {
        intermediates.forEach(i -> {
            HashSet<AbstractLeftValue> atomUseSet = new HashSet<>();
            i.pushUpUse(atomUseSet);
            atomUseSet.stream().filter(j -> !defSet.contains(j)).forEach(useSet::add);
            i.pushUpDef(defSet);
        });
    }

    protected HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> getGenSet() {
        return genSet;
    }

    protected void pushUpGen() {
        HashSet<AbstractLeftValue> match = new HashSet<>();
        for (int i = intermediates.size() - 1; i >= 0; i--) {
            HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
            AbstractElement element = intermediates.get(i);
            element.pushUpDef(atomDefSet);
            for (AbstractLeftValue j : atomDefSet) {
                if (!match.contains(j)) {
                    genSet.add(new Pair<>(j, new Pair<>(this, i)));
                    match.add(j);
                }
            }
        }
    }

    protected void pushUpParams(Function function) {
        for (int i = 0; i < function.getParams(); i++) {
            genSet.add(new Pair<>(function.getParam(i), new Pair<>(this, -1)));
        }
    }

    protected void pushUpKill(HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> otherGen) {
        HashSet<AbstractLeftValue> match = new HashSet<>();
        genSet.forEach(i -> match.add(i.getKey()));
        otherGen.stream().filter(i -> match.contains(i.getKey())).forEach(killSet::add);
    }

    protected boolean pushUpArriveInOut() {
        arriveInSet.clear();
        predecessor.forEach(i -> arriveInSet.addAll(i.arriveOutSet));
        HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> newOut = new HashSet<>(arriveInSet);
        newOut.removeAll(killSet);
        newOut.addAll(genSet);
        if (newOut.equals(arriveOutSet)) {
            return false;
        }
        arriveOutSet.clear();
        arriveOutSet.addAll(newOut);
        return true;
    }

    protected boolean pushUpActiveInOut() {
        activeOutSet.clear();
        successor.forEach(i -> activeOutSet.addAll(i.activeInSet));
        HashSet<AbstractLeftValue> newIn = new HashSet<>(activeOutSet);
        newIn.removeAll(defSet);
        newIn.addAll(useSet);
        if (newIn.equals(activeInSet)) {
            return false;
        }
        activeInSet.clear();
        activeInSet.addAll(newIn);
        return true;
    }

    protected void countVariableUsage() {
        intermediates.forEach(AbstractElement::countVariableUsage);
    }

    protected void buildConflictGraph(ConflictGraph conflictGraph) {
        activeInSet.forEach(i -> activeInSet.forEach(j -> conflictGraph.link(i, j)));
        HashSet<AbstractLeftValue> varSet = new HashSet<>(activeOutSet);
        for (int i = intermediates.size() - 1; i >= 0; i--) {
            HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
            intermediates.get(i).pushUpDef(atomDefSet);
            conflictGraph.insertNodes(atomDefSet);
            HashSet<AbstractLeftValue> atomUseSet = new HashSet<>();
            intermediates.get(i).pushUpUse(atomUseSet);
            conflictGraph.insertNodes(atomUseSet);
            atomDefSet.forEach(j -> varSet.forEach(k -> conflictGraph.link(j, k)));
            varSet.removeAll(atomDefSet);
            varSet.addAll(atomUseSet);
        }
    }

    protected void updateSpreadAble(HashMap<Pair<AbstractLeftValue
            , Pair<BasicBlock, Integer>>, AbstractVariable> defineMap) {
        for (int i = 0; i < intermediates.size(); i++) {
            if (intermediates.get(i) instanceof CalculateElement) {
                AbstractVariable spread = ((CalculateElement)intermediates.get(i)).isSpreadAble();
                if (spread != null) {
                    HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
                    intermediates.get(i).pushUpDef(atomDefSet);
                    for (AbstractLeftValue j : atomDefSet) {
                        if (!(j instanceof GlobalVariable)) {
                            defineMap.put(new Pair<>(j, new Pair<>(this, i)), spread);
                        }
                    }
                }
            }
        }
    }

    private Pair<AbstractLeftValue, Pair<BasicBlock, Integer>> getValidSpreader(AbstractLeftValue src
            , HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> varSet) {
        HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> select =
                (HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>>)
                varSet.stream().filter(i -> i.getKey() == src)
                .collect(Collectors.toSet());
        if (select.size() != 1) {
            return null;
        }
        return select.iterator().next();
    }

    private void removeDefFrom(AbstractElement element
            , HashMap<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>, AbstractVariable> defineMap) {
        HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
        element.pushUpDef(atomDefSet);
        HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> remove = new HashSet<>();
        defineMap.entrySet().stream().filter(i -> atomDefSet
                .contains(i.getKey().getKey())).forEach(i -> remove.add(i.getKey()));
        remove.forEach(defineMap::remove);
    }

    protected boolean variableSpread(HashMap<Pair<AbstractLeftValue
            , Pair<BasicBlock, Integer>>, AbstractVariable> defineMap) {
        boolean result = false;
        HashSet<Pair<AbstractLeftValue, Pair<BasicBlock, Integer>>> varSet = new HashSet<>(arriveInSet);
        for (int i = 0; i < intermediates.size(); i++) {
            HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
            intermediates.get(i).pushUpDef(atomDefSet);
            HashSet<AbstractLeftValue> atomUseSet = new HashSet<>();
            intermediates.get(i).pushUpUse(atomUseSet);
            for (AbstractLeftValue j : atomUseSet) {
                Pair<AbstractLeftValue, Pair<BasicBlock, Integer>> valid = getValidSpreader(j, varSet);
                if (valid != null && defineMap.containsKey(valid)) {
                    result = true;
                    intermediates.get(i).replaceVariable(j, defineMap.get(valid));
                    removeDefFrom(intermediates.get(i), defineMap);
                }
            }
            for (AbstractLeftValue j : atomDefSet) {
                varSet.removeIf(k -> k.getKey() == j);
                varSet.add(new Pair<>(j, new Pair<>(this, i)));
            }
        }
        return result;
    }

    protected boolean deadCodeElimination() {
        HashSet<Integer> remove = new HashSet<>();
        if (Settings.deadCodeElimination) {
            HashSet<AbstractLeftValue> varSet = new HashSet<>(activeOutSet);
            for (int i = intermediates.size() - 1; i >= 0; i--) {
                HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
                intermediates.get(i).pushUpDef(atomDefSet);
                HashSet<AbstractLeftValue> atomUseSet = new HashSet<>();
                intermediates.get(i).pushUpUse(atomUseSet);
                if (!atomDefSet.isEmpty() && !intermediates.get(i).getName().equals("scan")
                        && atomDefSet.stream().noneMatch(j ->
                        j instanceof GlobalVariable || varSet.contains(j))) {
                    remove.add(i);
                }
                else {
                    varSet.removeAll(atomDefSet);
                    varSet.addAll(atomUseSet);
                }
            }
            if (!remove.isEmpty()) {
                ArrayList<AbstractElement> newIntermediates = new ArrayList<>();
                for (int i = 0; i < intermediates.size(); i++) {
                    if (!remove.contains(i)) {
                        newIntermediates.add(intermediates.get(i));
                    }
                }
                intermediates = newIntermediates;
            }
        }
        return !remove.isEmpty();
    }

    protected void branchInstructionOptimize() {
        BranchOptimizeTemplate.optimize(intermediates);
    }

    protected void executeIndex(int index, Function function, VirtualRunner runner) {
        if (Settings.codeSimulateTraceToStdout) {
            String functionName = function == null ? "pre_main" : function.toString();
            System.out.println("running @function " + functionName + " -> #basic_block " + blockId
                    + " : " + intermediates.get(index).displayExecute(runner));
        }
        intermediates.get(index).execute(runner);
    }

    protected int pushUpSpace(int preSpace) {
        int space = preSpace;
        for (AbstractElement element : intermediates) {
            int addSpace = element.getSpace();
            element.setAddress(space);
            space += addSpace;
        }
        return space;
    }

    protected void pushUpFunctionCall() {
        HashSet<AbstractLeftValue> varSet = new HashSet<>(activeOutSet);
        for (int i = intermediates.size() - 1; i >= 0; i--) {
            HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
            intermediates.get(i).pushUpDef(atomDefSet);
            HashSet<AbstractLeftValue> atomUseSet = new HashSet<>();
            intermediates.get(i).pushUpUse(atomUseSet);
            varSet.removeAll(atomDefSet);
            varSet.addAll(atomUseSet);
            intermediates.get(i).setPlaceInfo(this, i);
            if (intermediates.get(i).getMain() instanceof Function) {
                ((Function)intermediates.get(i).getMain()).updateActives(this, i, varSet);
            }
        }
    }

    private void pushUpVariablePlace() {
        for (int i = 0; i < intermediates.size(); i++) {
            HashSet<AbstractLeftValue> atomDefSet = new HashSet<>();
            intermediates.get(i).pushUpDef(atomDefSet);
            HashSet<AbstractLeftValue> atomUseSet = new HashSet<>();
            intermediates.get(i).pushUpUse(atomUseSet);
            for (AbstractLeftValue j : atomDefSet) {
                if (!variablePlace.containsKey(j)) {
                    RedBlackTree<Integer> redBlackTree = new RedBlackTree<>();
                    redBlackTree.insert(i);
                    variablePlace.put(j, redBlackTree);
                }
                else {
                    variablePlace.get(j).insert(i);
                }
            }
            for (AbstractLeftValue j : atomUseSet) {
                if (!variablePlace.containsKey(j)) {
                    RedBlackTree<Integer> redBlackTree = new RedBlackTree<>();
                    redBlackTree.insert(i);
                    variablePlace.put(j, redBlackTree);
                }
                else {
                    variablePlace.get(j).insert(i);
                }
            }
        }
    }

    protected void toAssembly(Function function, MipsAssembly mipsAssembly) {
        if (Settings.optTempRegisterOptimize) {
            pushUpVariablePlace();
        }
        mipsAssembly.processClearLocalRegister();
        mipsAssembly.processComment("#basic_block " + blockId, 2);
        blockTags.forEach(i -> AbstractElement.getTagElement(i).toMipsAssembly(mipsAssembly));
        if (!Settings.preCalculateDataField || function != null) {
            for (int i = 0; i < intermediates.size(); i++) {
                if (Settings.processToStdout) {
                    String functionName = function == null ? "pre_main" : function.toString();
                    System.out.println("@function " + functionName + " -> #basic_block "
                            + blockId + " : processing " + intermediates.get(i));
                }
                mipsAssembly.processComment(intermediates.get(i).toString(), 3);
                intermediates.get(i).setPlaceInfo(this, i);
                intermediates.get(i).toMipsAssembly(mipsAssembly);
            }
            if (Settings.optTempRegisterOptimize && !intermediates.isEmpty()) {
                AbstractElement endElement = intermediates.get(intermediates.size() - 1);
                if (!(endElement instanceof BranchElement)
                        && !endElement.getName().equals("jmp")
                        && !endElement.getName().equals("exit")) {
                    mipsAssembly.processFlushLocalRegister(activeOutSet);
                    mipsAssembly.processClearLocalRegister();
                }
            }
        }
        else {
            for (int i = 0; i < intermediates.size(); i++) {
                if (!intermediates.get(i).getName().equals("alloc")) {
                    continue;
                }
                if (Settings.processToStdout) {
                    System.out.println("@function pre_main -> #basic_block "
                            + blockId + " : processing " + intermediates.get(i));
                }
                mipsAssembly.processComment(intermediates.get(i).toString(), 3);
                intermediates.get(i).setPlaceInfo(this, i);
                intermediates.get(i).toMipsAssembly(mipsAssembly);
            }
            if (Settings.optTempRegisterOptimize) {
                mipsAssembly.processFlushLocalRegister();
                mipsAssembly.processClearLocalRegister();
            }
        }
    }

    private void dumpItem(String str) {
        if (Settings.intermediateCodeToFile) {
            LogBuffer.buffer.add("\t" + str);
        }
        if (Settings.intermediateCodeToStdout) {
            System.out.println("\t" + str);
        }
    }

    protected void dump() {
        dumpItem("#basic_block " + blockId + ":");
        blockTags.forEach(i -> dumpItem("\t" + i + ":"));
        intermediates.forEach(i -> dumpItem("\t" + i));
    }
}