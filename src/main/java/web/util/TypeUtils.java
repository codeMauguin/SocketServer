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
        int dest;
        boolean negative = false;
        byte read = lexec.read();
        if (read == '-') {
            negative = true;
            read = lexec.read();
        }
        while (true) {
            if (read == ' ')
                continue;
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
        float integerPart = 0;
        float dest;
        float decimalPart = 0;
        boolean negative = false;
        int mode = 0;
        byte read = lexec.read();
        int index = 1;
        if (read == '-') {
            negative = true;
            read = lexec.read();
        }
        while (read != -1) {
            if (read == ' ')
                continue;
            if (read == '.') {
                if (mode == 1)
                    throw new IllegalArgumentException("解析到多个小数点");
                mode = 1;
                integerPart = negative ? integerPart : -integerPart;
                read = lexec.read();
            }
            if (mode == 0) {
                dest = ((char) (read & 0xff)) - 48;
                integerPart -= dest;
                read = lexec.read();
                if (read != -1 & read != '.')
                    integerPart *= 10;
            } else {
                double t = (char) (read & 0xff) - 48;
                decimalPart += t / (Math.pow(10, index++));
                read = lexec.read();
            }
        }
        return negative ? (integerPart - decimalPart) : (integerPart + decimalPart);
    }

    public static Double caseDouble(MessageReader.lexec lexec) {
        double integerPart = 0;
        double dest;
        double decimalPart = 0;
        boolean negative = false;
        int mode = 0;
        byte read = lexec.read();
        int index = 1;
        if (read == '-') {
            negative = true;
            read = lexec.read();
        }
        while (read != -1) {
            if (read == ' ')
                continue;
            if (read == '.') {
                if (mode == 1)
                    throw new IllegalArgumentException("解析到多个小数点");
                mode = 1;
                integerPart = negative ? integerPart : -integerPart;
                read = lexec.read();
            }
            if (mode == 0) {
                dest = ((char) (read & 0xff)) - 48;
                integerPart -= dest;
                read = lexec.read();
                if (read != -1 & read != '.')
                    integerPart *= 10;
            } else {
                double t = (char) (read & 0xff) - 48;
                decimalPart += t / (Math.pow(10, index++));
                read = lexec.read();
            }
        }
        return negative ? (integerPart - decimalPart) : (integerPart + decimalPart);
    }

    public static Byte caseByte(MessageReader.lexec lexec) {
        return lexec.read();
    }

    public static Long caseLong(MessageReader.lexec lexec) {
        Long res = 0L;
        Long dest = 0L;
        boolean negative = false;
        byte read = lexec.read();
        if (read == '-') {
            negative = true;
            read = lexec.read();
        }
        while (true) {
            dest = ((char) (read & 0xff)) - 48L;
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

    public static Short caseShort(MessageReader.lexec lexec) {
        Short res = 0;
        Short dest;
        boolean negative = false;
        byte read = lexec.read();
        if (read == '-') {
            negative = true;
            read = lexec.read();
        }
        while (true) {
            if (read == ' ')
                continue;
            dest = (short) (((char) (read & 0xff)) - 48);
            res = (short) (res - dest);
            read = lexec.read();
            if (read == '-') {
                throw new IllegalArgumentException("-解析错误");
            }
            if (read != -1)
                res = (short) (res * 10);
            else
                break;
        }
        return negative ? res : (short) -res;
    }
}
