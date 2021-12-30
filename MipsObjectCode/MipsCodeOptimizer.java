package MipsObjectCode;

import Global.Settings;
import java.util.HashSet;
import java.util.ArrayList;
import MipsObjectCode.InstructionSet.AbstractOther;
import MipsObjectCode.InstructionSet.AbstractBranch;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsCodeOptimizer {
    private static boolean endUp(HashSet<Integer> remove, ArrayList<AbstractInstruction> objectCode) {
        if (remove.isEmpty()) {
            return false;
        }
        ArrayList<AbstractInstruction> pushUp = new ArrayList<>();
        for (int i = 0; i < objectCode.size(); i++) {
            if (!remove.contains(i)) {
                pushUp.add(objectCode.get(i));
            }
        }
        objectCode.clear();
        objectCode.addAll(pushUp);
        return true;
    }

    protected static boolean optimizeLoadStore(ArrayList<AbstractInstruction> objectCode) {
        if (!Settings.removeUselessLoadStore) {
            return false;
        }
        HashSet<Integer> remove = new HashSet<>();
        for (int i = 0; i < objectCode.size(); i++) {
            if (objectCode.get(i).getName().equals("lw")) {
                for (int j = i + 1; j < objectCode.size(); j++) {
                    if (objectCode.get(j).getName().equals("lw")
                            && objectCode.get(i).toString().equals(objectCode.get(j).toString())) {
                        remove.add(j);
                    }
                    else if (!objectCode.get(j).getName().equals("#")) {
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < objectCode.size(); i++) {
            if (objectCode.get(i).getName().equals("sw")) {
                for (int j = i + 1; j < objectCode.size(); j++) {
                    if (objectCode.get(j).getName().equals("sw")
                            && objectCode.get(i).toString().equals(objectCode.get(j).toString())) {
                        remove.add(j);
                    }
                    else if (!objectCode.get(j).getName().equals("#")) {
                        break;
                    }
                }
            }
        }
        for (int i = objectCode.size() - 1; i >= 0; i--) {
            if (objectCode.get(i).getName().equals("lw")) {
                for (int j = i - 1; j >= 0; j--) {
                    if (objectCode.get(j).getName().equals("sw")
                            && objectCode.get(i).toString().substring(1)
                            .equals(objectCode.get(j).toString().substring(1))) {
                        remove.add(i);
                    }
                    else if (!objectCode.get(j).getName().equals("#")) {
                        break;
                    }
                }
            }
        }
        return endUp(remove, objectCode);
    }

    protected static boolean optimizeJumpTag(ArrayList<AbstractInstruction> objectCode) {
        if (!Settings.removeUselessJumpAndTag) {
            return false;
        }
        HashSet<Integer> remove = new HashSet<>();
        for (int i = 0; i < objectCode.size(); i++) {
            if (objectCode.get(i).getName().equals("j")) {
                String tag = ((AbstractOther)objectCode.get(i)).getTag();
                for (int j = i + 1; j < objectCode.size(); j++) {
                    if (objectCode.get(j).getName().endsWith(":")) {
                        if (objectCode.get(j).getName().split(":")[0].equals(tag)) {
                            remove.add(i);
                        }
                    }
                    else if (!objectCode.get(j).getName().equals("#")) {
                        break;
                    }
                }
            }
        }
        HashSet<String> usedTags = new HashSet<>();
        for (int i = 0; i < objectCode.size(); i++) {
            if (!remove.contains(i)) {
                if (objectCode.get(i) instanceof AbstractOther
                        && ((AbstractOther)objectCode.get(i)).getTag() != null) {
                    usedTags.add(((AbstractOther)objectCode.get(i)).getTag());
                }
                else if (objectCode.get(i) instanceof AbstractBranch
                        && ((AbstractBranch)objectCode.get(i)).getTag() != null) {
                    usedTags.add(((AbstractBranch)objectCode.get(i)).getTag());
                }
            }
        }
        for (int i = 0; i < objectCode.size(); i++) {
            if (objectCode.get(i).getName().endsWith(":")) {
                if (!usedTags.contains(objectCode.get(i).getName().split(":")[0])) {
                    remove.add(i);
                }
            }
        }
        return endUp(remove, objectCode);
    }
}