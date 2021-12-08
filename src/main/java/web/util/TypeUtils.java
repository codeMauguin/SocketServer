package web.util;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 4:09 下午 2021/12/8
 * @Modified By:
 */
public class TypeUtils {
    public static Boolean caseBoolean(MessageReader.lexec lexec) {
        if (lexec.limit() == 1) {
            int res = caseCharacter(lexec) - 48;
            if (res == 1) {
                return Boolean.TRUE;
            } else return Boolean.FALSE;
        }
        String res = new String(lexec.readAllBytes());
        if (res.equalsIgnoreCase("true"))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public static Integer caseInteger(MessageReader.lexec lexec) {
        int res = 0;
        int dest = 0;
        boolean negative = false;
        byte read = lexec.read();
        if (read == '-') {
            negative = true;
            read = lexec.read();
        }
        while (true) {
            dest = ((char) (read & 0xff)) - 48;
            res -= dest;
            read = lexec.read();
            if (read == '-') {
                throw new IllegalArgumentException("-解析错误");
            }
            if (read != -1)
                res *= 10;
            else
                break;
        }
        return negative ? res : -res;
    }

    public static Character caseCharacter(MessageReader.lexec lexec) {
        return (char) (lexec.read() & 0xff);
    }

    public static Float caseFloat(MessageReader.lexec lexec) {
        return null;
    }

    public static Double caseDouble(MessageReader.lexec lexec) {
        return null;
    }

    public static Byte caseByte(MessageReader.lexec lexec) {
        return lexec.read();
    }

    public static Long caseLong(MessageReader.lexec lexec) {
        return null;
    }

    public static Short caseShort(MessageReader.lexec lexec) {
        return null;
    }
}
