package WordAnalyse.Word;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Word {
    private final int line;
    private final int endIndex;
    protected String sourceWord;
    private final int startIndex;

    public static Word getWord(String in, int line, int index) {
        Pattern pattern = Pattern.compile(Ident.PATTERN);
        Matcher matcher = pattern.matcher(in);
        if (matcher.find(index) && matcher.start() == index) {
            String match = matcher.group();
            int end = matcher.end();
            pattern = Pattern.compile(Reserved.PATTERN);
            matcher = pattern.matcher(match);
            return matcher.matches() ? new Reserved(line, index, end, match) : new Ident(line, index, end, match);
        }
        pattern = Pattern.compile(FormatStr.PATTERN);
        matcher = pattern.matcher(in);
        if (matcher.find(index) && matcher.start() == index) {
            return new FormatStr(line, index, matcher.end(), matcher.group());
        }
        pattern = Pattern.compile(IntConst.PATTERN);
        matcher = pattern.matcher(in);
        if (matcher.find(index) && matcher.start() == index) {
            return new IntConst(line, index, matcher.end(), matcher.group());
        }
        pattern = Pattern.compile(Delimiter.PATTERN);
        matcher = pattern.matcher(in);
        if (matcher.find(index) && matcher.start() == index) {
            return new Delimiter(line, index, matcher.end(), matcher.group());
        }
        return null;
    }

    protected Word(int line, int startIndex, int endIndex, String sourceWord) {
        this.line = line + 1;
        this.endIndex = endIndex;
        this.startIndex = startIndex;
        this.sourceWord = sourceWord;
    }

    public abstract String getType();

    public abstract String getSourceWord();

    public int getLine() {
        return line;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public String toString() {
        return getType() + ' ' + getSourceWord();
    }
}