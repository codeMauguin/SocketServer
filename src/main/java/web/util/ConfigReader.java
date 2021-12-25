package web.util;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 5:28 下午 2021/12/6
 * @Modified By:
 */
public class ConfigReader {

    private final char EDF = 0xff;
    private final char comma = ',';
    private final char colon = ':';
    private final char errChar = ';';
    private String buffer;
    private int pos = 0;
    private int mark = 0;
    private int mode = 0;
    private byte[] input;

    public ConfigReader(byte[] input) {
        mode = 1;
        this.input = input;
    }

    public ConfigReader(String buffer) {
        this.buffer = buffer;
    }

    public void mark() {
        this.mark = pos;
    }

    public void reset() {
        this.pos = mark;
    }

    public char read() {
        return mode == 0 ? pos >= buffer.length() ? EDF : buffer.charAt(pos++) : pos >= input.length ? EDF :
                (char) (input[pos++] & EDF);
    }

    public String[] readArray() {
        StringBuilder key = new StringBuilder();
        String[] result = new String[5];
        int index = 0;
        int start = 0;
        P1:
        while (true) {
            char read = read();
            switch (read) {
                case '[' -> start++;
                case errChar -> throw new IllegalArgumentException("格式错误");
                case ']' -> {
                    if (start == 0) {
                        throw new IllegalArgumentException("格式不正确");
                    }
                    if (result.length == index) {
                        result = Arrays.copyOf(result, result.length + 1);
                    }
                    if (key.length() > 0)
                        result[index++] = key.toString();
                    break P1;
                }
                case comma -> {
                    if (result.length == index) {
                        result = Arrays.copyOf(result, result.length + (result.length >> 1));
                    }
                    result[index++] = key.toString();
                    key.setLength(0);
                }
                default -> key.append(read);
            }
        }
        return Arrays.copyOf(result, index);
    }


    public Map<String, Object> readMap() {
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        Map<String, Object> result = new LinkedHashMap<>();
        /*
        0：key
        1: value
         */
        int brand = 0;
        int startSum = 0;
        int ennSum = 0;
        int actual = 0;
        P1:
        while (true) {
            char read = read();
            final char start = '{';
            final char end = '}';
            switch (read) {
                case EDF -> {
                    break P1;
                }
                case '[' -> {
                    pos--;
                    result.put(key.toString(), readArray());
                    key.setLength(0);
                    brand = 0;
                }
                case '\"' -> actual++;
                case errChar -> throw new
                        IllegalArgumentException(MessageFormat.format("syntax error, position at {0}, name {1}",
                        result.size(), key.toString()));
                case start -> {
                    if (startSum > 0) {
                        pos--;
                        result.put(key.toString(), readMap());
                        key.setLength(0);
                    }
                    startSum++;
                }
                case end -> {
                    ennSum++;
                    if (startSum == ennSum) {
                        if (brand == 1)
                            result.put(key.toString(), value.toString());
                        break P1;
                    }
                }
                case colon -> {
                    if (actual % 2 != 0) {
                        throw new IllegalArgumentException(" expect ':' at %d, actual \"".formatted(result.size()));
                    }
                    brand = 1;
                }
                case comma -> {
                    if (actual % 2 != 0) {
                        throw new IllegalArgumentException(" expect ':' at %d, actual \"".formatted(result.size()));
                    }
                    brand = 0;
                    result.put(key.toString(), value.toString());
                    key.setLength(0);
                    value.setLength(0);
                }
                default -> {
                    switch (brand) {
                        case 0 -> key.append(read);
                        case 1 -> value.append(read);
                    }
                }
            }
        }
        return result;
    }


    public void read(BiConsumer<String, String> action) {
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        int p = 0;
        int mid = 0;
        int end = 0;
        P1:
        while (true) {
            char read = read();
            switch (read) {
                case EDF -> {
                    if (p != 0) {
                        action.accept(key.toString(), value.toString());
                    }
                    break P1;
                }
                case '[' -> {
                    p = 0;
                    end += 1;
                    pos--;
                    for (String var0 : readArray()) {
                        action.accept(key.toString(), var0);
                    }
                    key.setLength(0);
                    value.setLength(0);
                    pos++;
                }
                case ' ' -> {
                }
                case comma, errChar -> {
                    p = 0;
                    end += 1;
                    action.accept(key.toString(), value.toString());
                    key.setLength(0);
                    value.setLength(0);
                }
                case colon -> {
                    mid += 1;
                    p = 1;
                }
                default -> {
                    if (p == 0 && mid == end) {
                        key.append(read);
                    } else if (p == 1 && ((mid - end) == 1)) {
                        value.append(read);
                    }
                }
            }
        }


    }

}
