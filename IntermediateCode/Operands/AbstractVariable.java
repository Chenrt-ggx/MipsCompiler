package IntermediateCode.Operands;

import java.util.HashMap;
import MipsObjectCode.MipsAssembly;

public abstract class AbstractVariable extends AbstractOperand {
    protected static final int TEMP = 5;
    protected static final int CONST = 0;
    protected static final int PARAM = 3;
    protected static final int GLOBAL = 2;
    protected static final int VARIABLE = 4;
    protected static final int RETURN_VAL = 1;
    private static final HashMap<Integer, String> mapToString;

    static {
        mapToString = new HashMap<>();
        mapToString.put(TEMP, "temp_");
        mapToString.put(CONST, "const_");
        mapToString.put(PARAM, "param_");
        mapToString.put(GLOBAL, "global_");
        mapToString.put(VARIABLE, "var_");
        mapToString.put(RETURN_VAL, "ret_");
    }

    public AbstractVariable(int type, int id) {
        super(mapToString.get(type) + id);
    }

    public abstract int loadToRegister(MipsAssembly assembly);

    public abstract void loadToRegister(int dst, MipsAssembly assembly);
}