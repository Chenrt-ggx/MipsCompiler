package IntermediateCode;

import java.util.Map;
import Global.Settings;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;
import MipsObjectCode.MipsAssembly;
import IntermediateCode.Operands.ParamVariable;
import IntermediateCode.Operands.NormalVariable;
import IntermediateCode.Operands.AbstractLeftValue;

public class ConflictGraph {
    private final TreeSet<AbstractLeftValue> nodeSet;
    private final ArrayList<AbstractLeftValue> memoryList;
    private final ArrayList<AbstractLeftValue> registerList;
    private final HashMap<AbstractLeftValue, HashSet<AbstractLeftValue>> graph;

    protected ConflictGraph() {
        graph = new HashMap<>();
        nodeSet = new TreeSet<>();
        memoryList = new ArrayList<>();
        registerList = new ArrayList<>();
    }

    private void insertNode(AbstractLeftValue abstractLeftValue) {
        nodeSet.add(abstractLeftValue);
        graph.put(abstractLeftValue, new HashSet<>());
    }

    private boolean isValidLeftValue(AbstractLeftValue leftValue) {
        if (Settings.colorIncludeParamRegister) {
            return leftValue instanceof NormalVariable || leftValue instanceof ParamVariable;
        }
        else {
            return leftValue instanceof NormalVariable;
        }
    }

    protected void insertNodes(HashSet<AbstractLeftValue> nodes) {
        nodes.stream().filter(i -> !graph.containsKey(i)
                && isValidLeftValue(i)).forEach(this::insertNode);
    }

    protected void link(AbstractLeftValue src, AbstractLeftValue dst) {
        if (isValidLeftValue(src) && isValidLeftValue(dst) && !src.equals(dst)) {
            if (!graph.containsKey(src)) {
                insertNode(src);
            }
            if (!graph.containsKey(dst)) {
                insertNode(dst);
            }
            graph.get(src).add(dst);
            graph.get(dst).add(src);
        }
    }

    private void fillList(HashMap<AbstractLeftValue, Integer> outCount) {
        graph.forEach((k, v) -> outCount.put(k, v.size()));
        outCount.forEach(AbstractLeftValue::updateConflicts);
        while (registerList.size() + memoryList.size() < graph.size()) {
            Map.Entry<AbstractLeftValue, Integer> entry = outCount.entrySet()
                     .stream().filter(i -> nodeSet.contains(i.getKey()))
                    .min(Map.Entry.comparingByValue()).orElse(null);
            if (entry == null) {
                System.err.println("unexpected null entry appeared when coloring node");
                continue;
            }
            if (entry.getValue() < MipsAssembly.globalRegisterPool.length) {
                graph.get(entry.getKey()).forEach(i -> outCount.compute(i, (k, v) -> v -= 1));
                registerList.add(entry.getKey());
                nodeSet.remove(entry.getKey());
                continue;
            }
            AbstractLeftValue toRemove = nodeSet.iterator().next();
            graph.get(toRemove).forEach(i -> outCount.compute(i, (k, v) -> v -= 1));
            memoryList.add(toRemove);
            nodeSet.remove(toRemove);
        }
    }

    protected void colorNode() {
        fillList(new HashMap<>());
        HashMap<AbstractLeftValue, HashSet<Integer>> validColor = new HashMap<>();
        registerList.forEach(i -> {
            HashSet<Integer> colors = new HashSet<>();
            for (int j : MipsAssembly.globalRegisterPool) {
                colors.add(j);
            }
            validColor.put(i, colors);
        });
        registerList.stream().sorted((Comparator.comparing(i -> graph.get(i).size()).reversed())).forEach(i -> {
            int color = validColor.get(i).iterator().next();
            i.setRegister(color);
            graph.get(i).stream().filter(validColor::containsKey).forEach(j ->
                    validColor.get(j).remove(color));
        });
    }
}