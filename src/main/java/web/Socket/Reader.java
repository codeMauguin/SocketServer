package web.Socket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 4:29 下午 2021/12/4
 * @Modified By:
 */
class Reader {
    //    private final InputStream inputStream;
    private final byte[] EDF = new byte[]{'\r', '\n'};
    private final ReaderInputSteam inputStream;

    public Reader(ReaderInputSteam inputStream) {
        this.inputStream = inputStream;
    }

    @SuppressWarnings("all")
    public byte[] readByteArray(int size) throws IOException {
        byte[] buffer = new byte[size];
        inputStream.read(buffer, 0, size);
        return buffer;
    }


    public String readLine() throws IOException {
        int read = inputStream.read();
        if (read == -1) {
            return "";
        }
        int index = 1;
        byte[] buffer = new byte[10];
        byte[] temporaryBuffer = new byte[2];
        buffer[0] = (byte) read;
        if (read == 13) {
            inputStream.next(10);
            return "";
        }
        P1:
        while (true) {
            if (index + 1 >= buffer.length)
                buffer = expansion(buffer);
            inputStream.mark(2);
            int readLen = inputStream.read(temporaryBuffer, 0, 2);
            switch (readLen) {
                case -1:
                    break P1;
                case 2: {
                    buffer[index + 1] = temporaryBuffer[1];
                }
                case 1: {
                    buffer[index] = temporaryBuffer[0];
                }
                default:
                    index += readLen;
            }
            if (index >= 2) {
                if (buffer[index - 2] == '\n') {
                    if (Arrays.equals(buffer, index - 3, index - 1, EDF, 0, 2)) {
                        inputStream.reset();
                        inputStream.next();
                        index = index - 3;
                        break;
                    }
                } else if (buffer[index - 2] == '\r') {
                    if (Arrays.equals(buffer, index - 2, index, EDF, 0, 2)) {
                        index = index - 2;
                        break;
                    }
                }
            } else {
                // 0 1 2 3
                if (Arrays.equals(buffer, 0, 2, EDF, 0, 2)) {
                    return "";
                }
            }
        }
        return new String(buffer, 0, index, StandardCharsets.UTF_8);
    }

    private byte[] expansion(byte[] buffer) {
        return Arrays.copyOf(buffer, buffer.length * 2);
    }

}