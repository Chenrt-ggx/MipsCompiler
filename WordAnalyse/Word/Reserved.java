package WordAnalyse.Word;

import java.util.HashMap;
import java.util.Comparator;

public class Reserved extends Word {
    protected static final String PATTERN;
    private static final HashMap<String, String> reservedToType;

    static {
        reservedToType = new HashMap<>();
        reservedToType.put("main", "MAINTK");
        reservedToType.put("const", "CONSTTK");
        reservedToType.put("int", "INTTK");
        reservedToType.put("break", "BREAKTK");
        reservedToType.put("continue", "CONTINUETK");
        reservedToType.put("if", "IFTK");
        reservedToType.put("else", "ELSETK");
        reservedToType.put("while", "WHILETK");
        reservedToType.put("getint", "GETINTTK");
        reservedToType.put("printf", "PRINTFTK");
        reservedToType.put("return", "RETURNTK");
        reservedToType.put("void", "VOIDTK");
        StringBuilder builder = new StringBuilder();
        reservedToType.keySet().stream().sorted(Comparator.comparing(String::length)
                .reversed()).forEach(i -> builder.append(i).append('|'));
        PATTERN = builder.deleteCharAt(builder.length() - 1).toString();
    }

    private final String type;

    protected Reserved(int line, int startIndex, int endIndex, String word) {
        super(line, startIndex, endIndex, word);
        type = reservedToType.get(word);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getSourceWord() {
        return sourceWord;
    }
}