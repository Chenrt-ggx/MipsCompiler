import java.io.File;
import Global.Settings;
import Global.ErrorInfo;
import Global.LogBuffer;
import java.util.HashSet;
import java.util.Objects;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import WordAnalyse.WordAnalyzer;
import MipsObjectCode.MipsAssembly;
import GrammarAnalyse.CompUnitParser;
import IntermediateCode.VirtualRunner;
import IntermediateCode.IntermediateCode;
import IntermediateCode.IntermediateBuilder;

public class Compiler {
    private static ArrayList<String> readFile(File file) {
        if (!file.exists()) {
            System.out.println("fatal error: input file not exist");
            System.out.println("compilation terminated");
            System.out.println();
            System.exit(0);
        }
        ArrayList<String> lines = new ArrayList<>(1 << 6);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file), 1 << 8);
            for (String line; (line = reader.readLine()) != null;) {
                lines.add(line);
            }
            reader.close();
        }
        catch (IOException e) {
            System.err.println("failed to read file");
            e.printStackTrace();
        }
        return lines;
    }

    private static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (!file.isFile()) {
            for (File i : Objects.requireNonNull(file.listFiles())) {
                deleteFile(i);
            }
        }
        if (!file.delete()) {
            System.err.println("failed when removing " + file.getName());
        }
    }

    private static void runObjectCodeOnMars() {
        try {
            System.out.print("# Connected To " + Settings.marsName + " : ");
            String[] command = new String[]{"java", "-jar", Settings.marsName, "mips.txt"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.start().waitFor();
            File statistics = new File("statistics.txt");
            deleteFile(statistics);
            if (!(new File("InstructionStatistics.txt")).renameTo(statistics)) {
                System.err.println("failed when renaming InstructionStatistics.txt");
            }
            System.out.println();
            System.out.println("# Instruction Statistics From " + Settings.marsName + " :");
            System.out.println();
            BufferedReader reader = new BufferedReader(new FileReader(statistics), 1 << 8);
            for (String str; (str = reader.readLine()) != null;) {
                str = str.split(":")[0].replaceAll("\\s", "") + " : "
                        + str.split(":")[1].replaceAll("\\s", "");
                System.out.println(str);
            }
            reader.close();
        }
        catch (IOException | InterruptedException e) {
            System.err.println("failed to connect " + Settings.marsName);
            e.printStackTrace();
        }
    }

    private static void runCompiler() {
        // System.setOut(new PrintStream("pcoderesult.txt"));
        Settings.selfCheck();
        Settings.useOptimizeMask(Settings.optimizeLevels[toValidOptimizeLevel(Settings.optimizeLevel)]);
        WordAnalyzer wordAnalyzer = new WordAnalyzer(readFile(new File(Settings.sourceCodeName)));
        CompUnitParser parser = new CompUnitParser(wordAnalyzer);
        if (Settings.syntaxTreeFormattedToStdout) {
            StringBuilder builder = new StringBuilder();
            parser.getTreeNode().display(builder);
            System.out.println(builder);
        }
        if (ErrorInfo.logIfHasError()) {
            LogBuffer.writeFile(Settings.errorFileName, LogBuffer.buffer);
            return;
        }
        parser.getTreeNode().optimize();
        IntermediateBuilder builder = new IntermediateBuilder();
        parser.getTreeNode().toIntermediate(builder);
        IntermediateCode intermediateCode = builder.toIntermediateCode();
        intermediateCode.optimize();
        intermediateCode.dump();
        ArrayList<Integer> dataField = null;
        if (Settings.preCalculateDataField) {
            dataField = (new VirtualRunner(intermediateCode
                    , 1 << 28)).getDataField();
        }
        if (Settings.intermediateCodeSimulate) {
            (new VirtualRunner(intermediateCode, 1 << 28)).run();
        }
        MipsAssembly mipsAssembly = new MipsAssembly(dataField);
        intermediateCode.toAssembly(mipsAssembly);
        mipsAssembly.optimize();
        mipsAssembly.dump();
        LogBuffer.writeFile(Settings.intermediateCodeName, LogBuffer.buffer);
        LogBuffer.writeFile(Settings.targetCodeName, LogBuffer.code);
        if (Settings.runObjectCodeOnMars) {
            runObjectCodeOnMars();
        }
    }

    private static int toValidOptimizeLevel(String source) {
        if (source == null || source.equals("-O0")) {
            return 0;
        }
        else if (source.equals("-O1")) {
            return 1;
        }
        else if (source.equals("-O2")) {
            return 2;
        }
        else if (source.equals("-O3")) {
            return 3;
        }
        return -1;
    }

    private static boolean parseArgs(String[] args) {
        if (args.length < 1 || args.length > 10) {
            return false;
        }
        if (args[0].equals("-h") || args[0].equals("--help")) {
            return false;
        }
        if (args[0].equals("-v") || args[0].equals("--version")) {
            System.out.println(Settings.compilerName + " " + Settings.version);
            System.out.println();
            System.exit(0);
        }
        Settings.sourceCodeName = args[0];
        for (int i = 1; i < args.length - 1; i++) {
            if (!args[i].startsWith("-") && !args[i + 1].startsWith("-")) {
                return false;
            }
        }
        HashSet<String> out = new HashSet<>();
        Settings.intermediateCodeToFile = false;
        Settings.courseSpecificErrorToFile = false;
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (out.contains(args[i])) {
                    return false;
                }
                out.add(args[i]);
                if (args[i].equals("-h") || args[i].equals("--help")) {
                    return false;
                }
                if (args[i].equals("-v") || args[i].equals("--version")) {
                    System.out.println("Yu Ge Compiler " + Settings.version);
                    System.out.println();
                    System.exit(0);
                }
                else if (args[i].equals("-d") || args[i].equals("--debug")) {
                    if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                        Settings.intermediateCodeToFile = true;
                        Settings.courseSpecificErrorToFile = true;
                    }
                    else {
                        return false;
                    }
                }
                else if (args[i].startsWith("-O") && toValidOptimizeLevel(args[i]) >= 0) {
                    if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                        Settings.optimizeLevel = args[i];
                    }
                    else {
                        return false;
                    }
                }
                else if (args[i].equals("-o")) {
                    if (i + 1 < args.length) {
                        Settings.targetCodeName = args[i + 1];
                    }
                    else {
                        return false;
                    }
                    i++;
                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length == 0 || parseArgs(args)) {
            runCompiler();
        }
        else {
            System.out.println("Usage: <source-file> " +
                    "[-o <target-file>] [-O<level>] [-d | --debug] [-h | --help] [-v | --version]");
            System.out.println("    -o               " +
                    "set target file, default to mips.txt");
            System.out.println("    -O<level>        " +
                    "set optimization level, default non-optimization -O0");
            System.out.println("    -d, --debug      " +
                    "dump intermediate code to output.txt, dump error message to error.txt");
            System.out.println("    -v, --version    " +
                    "show version of " + Settings.compilerName);
            System.out.println("    -h, --help       " +
                    "show usage of " + Settings.compilerName);
            System.out.println("    <target-file>    " +
                    "target assembly file in mips");
            System.out.println("    <source-file>    " +
                    "source code file matching SysY grammar");
            System.out.println();
        }
    }
}