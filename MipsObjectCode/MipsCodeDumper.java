package MipsObjectCode;

import Global.Settings;
import Global.LogBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsCodeDumper {
    private final int globalSpace;
    private final ArrayList<Integer> dataField;
    private final LinkedList<String> initString;
    private final ArrayList<AbstractInstruction> objectCode;

    protected MipsCodeDumper(int globalSpace, LinkedList<String> initString
            , ArrayList<AbstractInstruction> objectCode, ArrayList<Integer> dataField) {
        this.dataField = dataField;
        this.initString = initString;
        this.objectCode = objectCode;
        this.globalSpace = globalSpace;
    }

    private void dumpData() {
        if (globalSpace >= 0 || !initString.isEmpty()) {
            if (Settings.objectCodeToFile) {
                LogBuffer.code.add(".data");
                if (globalSpace != 0) {
                    LogBuffer.code.add((Settings.objectCodeWithFormat ? "\t" : "") + "global:");
                    StringBuilder builder = new StringBuilder();
                    if (dataField != null) {
                        for (Integer i : dataField) {
                            builder.append(i).append(" ");
                            if (builder.length() > 100) {
                                LogBuffer.code.add((Settings.objectCodeWithFormat ? "\t" : "") + builder);
                                builder = new StringBuilder();
                            }
                        }
                    }
                    if (builder.length() > 0) {
                        LogBuffer.code.add((Settings.objectCodeWithFormat ? "\t" : "") + builder);
                    }
                    int space = (globalSpace + 1 - (dataField == null ? 0 : dataField.size())) << 2;
                    if (space != 0) {
                        LogBuffer.code.add((Settings.objectCodeWithFormat ? "\t" : "") + ".space " + space);
                    }
                }
                initString.forEach(i -> LogBuffer.code.add((Settings.objectCodeWithFormat ? "\t" : "") + i));
                LogBuffer.code.add("");
            }
            if (Settings.objectCodeToStdout) {
                System.out.println(".data");
                if (globalSpace != 0) {
                    System.out.println((Settings.objectCodeWithFormat ? "\t" : "") + "global:");
                    StringBuilder builder = new StringBuilder();
                    if (dataField != null) {
                        for (Integer i : dataField) {
                            builder.append(i).append(" ");
                            if (builder.length() > 100) {
                                System.out.println((Settings.objectCodeWithFormat ? "\t" : "") + builder);
                                builder = new StringBuilder();
                            }
                        }
                    }
                    if (builder.length() > 0) {
                        System.out.println((Settings.objectCodeWithFormat ? "\t" : "") + builder);
                    }
                    int space = (globalSpace + 1 - (dataField == null ? 0 : dataField.size())) << 2;
                    if (space != 0) {
                        System.out.println((Settings.objectCodeWithFormat ? "\t" : "") + ".space " + space);
                    }
                }
                initString.forEach(i -> System.out.println((Settings.objectCodeWithFormat ? "\t" : "") + i));
                System.out.println();
            }
        }
    }

    private void dumpText() {
        if (Settings.objectCodeToFile) {
            LogBuffer.code.add(".text");
        }
        if (Settings.objectCodeToStdout) {
            System.out.println(".text");
        }
        objectCode.stream().filter(i -> Settings.objectCodeWithFormat || !i.getName().equals("#")).forEach(i -> {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < i.getIndent() && Settings.objectCodeWithFormat; j++) {
                builder.append("\t");
            }
            builder.append(i);
            if (Settings.objectCodeToFile) {
                LogBuffer.code.add(builder.toString());
            }
            if (Settings.objectCodeToStdout) {
                System.out.println(builder.toString());
            }
        });
        if (Settings.objectCodeToFile) {
            LogBuffer.code.add("");
        }
        if (Settings.objectCodeToStdout) {
            System.out.println();
        }
    }

    protected void dump() {
        if (Settings.objectCodeToFile) {
            LogBuffer.code.add("# Powered By " + Settings.compilerName + " " + Settings.version);
            LogBuffer.code.add("");
        }
        dumpData();
        dumpText();
    }
}