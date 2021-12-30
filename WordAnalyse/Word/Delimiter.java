package WordAnalyse.Word;

import java.util.HashMap;
import java.util.Comparator;

public class Delimiter extends Word {
    protected static final String PATTERN;
    private static final HashMap<String, String> delimiterToType;
    private static final HashMap<String, String> delimiterToRegex;

    static {
        delimiterToType = new HashMap<>();
        delimiterToType.put("!", "NOT");
        delimiterToType.put("&&", "AND");
        delimiterToType.put("||", "OR");
        delimiterToType.put("+", "PLUS");
        delimiterToType.put("-", "MINU");
        delimiterToType.put("*", "MULT");
        delimiterToType.put("/", "DIV");
        delimiterToType.put("%", "MOD");
        delimiterToType.put("<", "LSS");
        delimiterToType.put("<=", "LEQ");
        delimiterToType.put(">", "GRE");
        delimiterToType.put(">=", "GEQ");
        delimiterToType.put("==", "EQL");
        delimiterToType.put("!=", "NEQ");
        delimiterToType.put("=", "ASSIGN");
        delimiterToType.put(";", "SEMICN");
        delimiterToType.put(",", "COMMA");
        delimiterToType.put("(", "LPARENT");
        delimiterToType.put(")", "RPARENT");
        delimiterToType.put("[", "LBRACK");
        delimiterToType.put("]", "RBRACK");
        delimiterToType.put("{", "LBRACE");
        delimiterToType.put("}", "RBRACE");
        delimiterToRegex = new HashMap<>();
        delimiterToRegex.put("||", "\\|\\|");
        delimiterToRegex.put("+", "\\+");
        delimiterToRegex.put("*", "\\*");
        delimiterToRegex.put("(", "\\(");
        delimiterToRegex.put(")", "\\)");
        delimiterToRegex.put("[", "\\[");
        delimiterToRegex.put("]", "\\]");
        delimiterToRegex.put("{", "\\{");
        delimiterToRegex.put("}", "\\}");
        StringBuilder builder = new StringBuilder();
        delimiterToType.keySet().stream().sorted(Comparator.comparing(String::length)
                .reversed()).forEach(i -> builder.append(delimiterToRegex.get(i) != null ?
                delimiterToRegex.get(i) : i).append('|'));
        PATTERN = builder.deleteCharAt(builder.length() - 1).toString();
    }

    private final String type;

    protected Delimiter(int line, int startIndex, int endIndex, String word) {
        super(line, startIndex, endIndex, word);
        type = delimiterToType.get(word);
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