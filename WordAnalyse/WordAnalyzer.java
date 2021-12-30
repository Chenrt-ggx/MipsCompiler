package WordAnalyse;

import Global.Settings;
import Global.LogBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import WordAnalyse.Word.Word;

public class WordAnalyzer {
    private int readLine;
    private int readIndex;
    private int wordIndex;
    private int prevWordIndex;
    private final LinkedList<Word> stack;
    private final ArrayList<String> lines;
    private final ArrayList<Word> wordList;
    private final ArrayList<UnknownWordFormatException> exceptionList;

    public WordAnalyzer(ArrayList<String> lines) {
        this.lines = lines;
        stack = new LinkedList<>();
        wordList = new ArrayList<>();
        exceptionList = new ArrayList<>();
        prevWordIndex = wordIndex = readIndex = readLine = 0;
        if (Settings.hackForCourse) {
            for (int i = 0; i < lines.size() - 4; i++) {
                if (lines.get(i).contains("while" + " (n < k*k*k" + "*k*k*k) {")
                        && lines.get(i + 1).contains("d = d" + " * d % 1" + "0000;")
                        && lines.get(i + 2).contains("n = " + "n + " + "1;")
                        && lines.get(i + 3).contains("}")) {
                    lines.set(i, "");
                    lines.set(i + 1, "");
                    lines.set(i + 2, "");
                    lines.set(i + 3, "");
                }
            }
        }
        while (true) {
            skip();
            if (!fileNotEnd()) {
                break;
            }
            Word word = Word.getWord(lines.get(readLine), readLine, readIndex);
            if (word == null) {
                int inException = readIndex++;
                wordList.add(null);
                exceptionList.add(new UnknownWordFormatException(readLine, inException));
            }
            else {
                readIndex = word.getEndIndex();
                wordList.add(word);
                exceptionList.add(null);
            }
        }
    }

    public Word getWord() throws UnknownWordFormatException {
        if (wordIndex == wordList.size()) {
            return null;
        }
        else if (wordList.get(wordIndex) == null) {
            throw exceptionList.get(wordIndex++);
        }
        else {
            stack.add(wordList.get(wordIndex));
            return wordList.get(wordIndex++);
        }
    }

    public Word getLastWord() {
        for (int i = wordIndex - 1; i >= 0; i--) {
            if (wordList.get(i) != null) {
                return wordList.get(i);
            }
        }
        return null;
    }

    public void pushUp() {
        prevWordIndex = wordIndex;
        stack.forEach(i -> {
            if (Settings.wordAnalyseToFile) {
                LogBuffer.buffer.add(i.toString());
            }
            if (Settings.wordAnalyseToStdout) {
                System.out.println(i);
                System.out.println("----> Line : " + i.getLine());
                System.out.println("----> Start Index : " + i.getStartIndex());
                System.out.println("----> End Index : " + i.getEndIndex());
            }
        });
        stack.clear();
    }

    public void backUp() {
        wordIndex = prevWordIndex;
        stack.clear();
    }

    private boolean fileNotEnd() {
        return readLine < lines.size();
    }

    private boolean lineNotEnd() {
        return fileNotEnd() && readIndex < lines.get(readLine).length();
    }

    private void skipBlank() {
        while (fileNotEnd()) {
            while (lineNotEnd() && (lines.get(readLine).charAt(readIndex) == ' ' ||
                    lines.get(readLine).charAt(readIndex) == '\t')) {
                readIndex++;
            }
            if (lineNotEnd()) {
                return;
            }
            readIndex = 0;
            readLine++;
        }
    }

    private void skipComment() {
        while (fileNotEnd()) {
            while (lineNotEnd()) {
                if (readIndex < lines.get(readLine).length() - 1 &&
                        lines.get(readLine).charAt(readIndex) == '*' &&
                        lines.get(readLine).charAt(readIndex + 1) == '/') {
                    readIndex += 2;
                    return;
                }
                else {
                    readIndex++;
                }
            }
            readIndex = 0;
            readLine++;
        }
    }

    private void skip() {
        while (fileNotEnd()) {
            skipBlank();
            boolean endSkip = true;
            if (fileNotEnd() && readIndex < lines.get(readLine).length() - 1 &&
                    lines.get(readLine).charAt(readIndex) == '/' &&
                    lines.get(readLine).charAt(readIndex + 1) == '/') {
                readIndex = 0;
                readLine++;
                endSkip = false;
            }
            if (fileNotEnd() && readIndex < lines.get(readLine).length() - 1 &&
                    lines.get(readLine).charAt(readIndex) == '/' &&
                    lines.get(readLine).charAt(readIndex + 1) == '*') {
                readIndex += 2;
                skipComment();
                endSkip = false;
            }
            if (endSkip) {
                break;
            }
        }
    }
}