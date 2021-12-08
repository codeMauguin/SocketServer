package web.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:48 下午 2021/12/8
 * @Modified By:
 */

public class MessageReader {
    private static final char EDF = 0xff;
    private static final char start = '{';
    private static final char comma = ',';

    private static final char preBank = '[';
    private static final char bankSuffix = ']';
    private static final char colon = ':';
    private static final char errChar = ';';
    private static final char end = '}';
    private final byte[] input;
    private final int limit;
    private int pos = 0;

    public MessageReader(byte[] input) {
        this.input = input;
        limit = input.length;
    }


    public static List<lexec> readList(lexec lexec) {
        List<lexec> result = new ArrayList<>();
        int start = 0;
        int end = 0;
        int pos = 0;
        int prePos = 0;
        P1:
        while (true) {
            byte read = lexec.read();
            switch (read) {
                case '"' -> {
                }
                case -1 -> {
                    break P1;
                }
                case preBank -> {
                    start++;
                    prePos = pos + 1;
                }
                case bankSuffix -> {
                    end++;
//                    [1,2,3]
                    if (start == end) {
                        MessageReader.lexec exe = new lexec(lexec.subArray(prePos, pos));
                        result.add(exe);
                        break P1;
                    }
                }
                case comma -> {
                    MessageReader.lexec exe = new lexec(lexec.subArray(prePos, pos));
                    result.add(exe);
                    prePos = pos + 1;
                }
            }
            pos++;
        }
        return result;
    }

    public char get() {
        return pos < limit ? (char) (input[pos++] & EDF) : EDF;
    }

    public Map<String, lexec> read() {
        StringBuilder key = new StringBuilder();
        Map<String, lexec> result = new LinkedHashMap<>();

        P1:
        while (true) {
            char c = get();
            switch (c) {
                case start, '\"' -> {
                }
                case comma, errChar -> {
                    throw new IllegalArgumentException("参数错误");
                }
                case colon -> {
                    int start = pos;
                    int next = findNext();
                    byte[] token = new byte[next - 1 - start];
                    System.arraycopy(input, start, token, 0, token.length);
                    result.put(key.toString(), new lexec(token));
                    key.setLength(0);
                }
                case end, EDF -> {
                    break P1;
                }
                default -> key.append(c);

            }

        }
        return result;
    }

    private int findNext() {
        boolean hasStart = false;
        char s = 0;
        char e = 0;
        int startSum = 0;
        int endSum = 0;
        char c = get();
        if (c == start) {
            s = start;
            e = end;
            startSum++;
            hasStart = true;
        } else if (c == preBank) {
            s = preBank;
            e = bankSuffix;
            startSum++;
            hasStart = true;
        }
        while (true) {
            c = get();
            if (hasStart) {
                if (c == s) {
                    startSum++;
                } else if (c == e) {
                    endSum++;
                    if (startSum == endSum) {
                        return ++pos;
                    }
                } else if (c == EDF) {
                    throw new IllegalArgumentException("没有闭合这个value");
                }
            } else if (c == comma) break;
            else if (c == EDF) {
                return ++pos;
            }

        }
        return pos;
    }

    public static class lexec {
        private final int limit;
        byte[] input;
        private int pos = 0;

        public lexec(byte[] input) {
            this.input = input;
            limit = input.length;
        }

        public byte[] subArray(int start, int end) {
            byte[] result = new byte[end - start];
            System.arraycopy(input, start, result, 0, end - start);
            return result;
        }

        public byte[] readAllBytes() {
            return input;
        }

        @Override
        public String toString() {
            return "lexec{" +
                    "limit=" + limit +
                    " input=" + new String(input) +
                    " pos=" + pos +
                    '}';
        }

        public byte read() {
            return pos < limit ? input[pos++] : -1;
        }

        public int limit() {
            return limit;
        }
    }


}
