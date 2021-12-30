package Global;

public class Settings {
    private static final class OptimizeLevel {
        private final boolean autoInlineFunction;
        private final boolean moveConstInFunctionToGlobal;
        private final boolean syntaxTreeExpressionOptimize;

        private final boolean deadCodeElimination;
        private final boolean constVariableSpread;
        private final boolean rewriteVariableSpread;
        private final boolean preCalculateDataField;
        private final boolean branchInstructionOptimize;

        private final boolean removeUselessLoadStore;
        private final boolean removeUselessJumpAndTag;
        private final boolean mulDivCalculateOptimize;
        private final boolean optTempRegisterOptimize;
        private final boolean colorIncludeParamRegister;
        private final boolean graphColoringRegisterAllocation;

        private OptimizeLevel(boolean autoInlineFunction, boolean moveConstInFunctionToGlobal
                , boolean syntaxTreeExpressionOptimize, boolean deadCodeElimination
                , boolean constVariableSpread, boolean rewriteVariableSpread
                , boolean preCalculateDataField, boolean branchInstructionOptimize
                , boolean removeUselessLoadStore, boolean removeUselessJumpAndTag
                , boolean mulDivCalculateOptimize, boolean optTempRegisterOptimize
                , boolean colorIncludeParamRegister, boolean graphColoringRegisterAllocation) {
            this.autoInlineFunction = autoInlineFunction;
            this.moveConstInFunctionToGlobal = moveConstInFunctionToGlobal;
            this.syntaxTreeExpressionOptimize = syntaxTreeExpressionOptimize;
            this.deadCodeElimination = deadCodeElimination;
            this.constVariableSpread = constVariableSpread;
            this.rewriteVariableSpread = rewriteVariableSpread;
            this.preCalculateDataField = preCalculateDataField;
            this.branchInstructionOptimize = branchInstructionOptimize;
            this.removeUselessLoadStore = removeUselessLoadStore;
            this.removeUselessJumpAndTag = removeUselessJumpAndTag;
            this.mulDivCalculateOptimize = mulDivCalculateOptimize;
            this.optTempRegisterOptimize = optTempRegisterOptimize;
            this.colorIncludeParamRegister = colorIncludeParamRegister;
            this.graphColoringRegisterAllocation = graphColoringRegisterAllocation;
        }
    }

    public static OptimizeLevel[] optimizeLevels = new OptimizeLevel[]{
        new OptimizeLevel(false, false, false
                , false, false, false
                , false, false, true
                , true, false, true
                , false, false),
        new OptimizeLevel(false, false, false
                , true, true, true
                , false, false, true
                , true, false, true
                , false, false),
        new OptimizeLevel(false, false, true
                , true, true, true
                , true, false, true
                , true, false, true
                , false, true),
        new OptimizeLevel(true, true, true
                , true, true, true
                , true, true, true
                , true, true, true
                , true, true)
    };

    public static boolean autoInlineFunction;
    public static boolean moveConstInFunctionToGlobal;
    public static boolean syntaxTreeExpressionOptimize;

    public static boolean deadCodeElimination;
    public static boolean constVariableSpread;
    public static boolean rewriteVariableSpread;
    public static boolean preCalculateDataField;
    public static boolean branchInstructionOptimize;

    public static boolean removeUselessLoadStore;
    public static boolean removeUselessJumpAndTag;
    public static boolean mulDivCalculateOptimize;
    public static boolean optTempRegisterOptimize;
    public static boolean colorIncludeParamRegister;
    public static boolean graphColoringRegisterAllocation;

    public static void useOptimizeMask(OptimizeLevel optimizeLevel) {
        autoInlineFunction = optimizeLevel.autoInlineFunction;
        moveConstInFunctionToGlobal = optimizeLevel.moveConstInFunctionToGlobal;
        syntaxTreeExpressionOptimize = optimizeLevel.syntaxTreeExpressionOptimize;
        deadCodeElimination = optimizeLevel.deadCodeElimination;
        constVariableSpread = optimizeLevel.constVariableSpread;
        rewriteVariableSpread = optimizeLevel.rewriteVariableSpread;
        preCalculateDataField = optimizeLevel.preCalculateDataField;
        branchInstructionOptimize = optimizeLevel.branchInstructionOptimize;
        removeUselessLoadStore = optimizeLevel.removeUselessLoadStore;
        removeUselessJumpAndTag = optimizeLevel.removeUselessJumpAndTag;
        mulDivCalculateOptimize = optimizeLevel.mulDivCalculateOptimize;
        optTempRegisterOptimize = optimizeLevel.optTempRegisterOptimize;
        colorIncludeParamRegister = optimizeLevel.colorIncludeParamRegister;
        graphColoringRegisterAllocation = optimizeLevel.graphColoringRegisterAllocation;
    }

    // -----------------------------------------------------------------------------------

    public static double version = 4.3;
    public static String marsName = "Mars_2021.jar";
    public static String compilerName = "Yu Ge Compiler";

    public static String optimizeLevel = "-O3";
    public static String errorFileName = "error.txt";
    public static String targetCodeName = "mips.txt";
    public static String sourceCodeName = "testfile.txt";
    public static String intermediateCodeName = "output.txt";

    public static boolean wordErrorToStderr = true;
    public static boolean wordAnalyseToFile = false;
    public static boolean wordAnalyseToStdout = false;

    public static boolean grammarErrorToStderr = true;
    public static boolean grammarAnalyseToFile = false;
    public static boolean grammarAnalyseToStdout = false;

    public static boolean syntaxTreeErrorToStderr = true;
    public static boolean courseSpecificErrorToFile = true;
    public static boolean syntaxTreeFormattedToStdout = false;
    public static boolean syntaxTreeConnectionSelfCheck = true;

    public static boolean intermediateCodeToFile = true;
    public static boolean intermediateCodeToStdout = false;
    public static boolean intermediateCodeSimulate = false;
    public static boolean codeSimulateTraceToStdout = false;

    public static boolean processToStdout = false;
    public static boolean objectCodeToFile = true;
    public static boolean objectCodeToStdout = false;
    public static boolean objectCodeWithFormat = true;

    public static boolean hackForCourse = true;
    public static boolean delayInjection = false;
    public static boolean runObjectCodeOnMars = false;

    public static void selfCheck() {
        if (intermediateCodeSimulate && runObjectCodeOnMars) {
            System.err.print("conflict setting : exec intermediate code and object code at same time");
            (new Exception()).printStackTrace();
            System.exit(0);
        }
    }
}