package web.Socket;

import web.Socket.InputStream.ReaderInputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 4:29 下午 2021/12/4
 * @Modified By:
 */
public class Reader {
    private final static String DEFAULT_RETURN = null;
    private final ReaderInputStream inputStream;
    private byte[] EDF = new byte[]{'\r', '\n'};
    private int size = 10;

    public Reader(ReaderInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setBufferLength(int size) {
        this.size = size;
    }

    public void setEDF(byte[] EDF) {
        this.EDF = EDF;
    }

    @SuppressWarnings("all")
    public byte[] readByteArray(int size) throws IOException {
        byte[] buffer = new byte[size];
        inputStream.read(buffer, 0, size);
        return buffer;
    }

    public String readLine() throws IOException {
        int read;
        read = inputStream.read();
        if (read == -1) {
            return DEFAULT_RETURN;
        }
        int index = 1;
        int state = 0;
        byte[] buffer = new byte[size];
        buffer[0] = (byte) read;
        for (; ; index++) {
            for (int i = 0; i < EDF.length; i++) {
                if (state == i && read == EDF[i]) {
                    state++;
                }
            }
            if (state == EDF.length) {
                break;
            }
            if (index >= buffer.length) buffer = expansion(buffer);
            read = inputStream.read();
            if (read == -1) {
                break;
            }
            buffer[index] = (byte) read;
        }
        return new String(buffer, 0, index - EDF.length, StandardCharsets.UTF_8);
    }

    private byte[] expansion(byte[] buffer) {
        return Arrays.copyOf(buffer, buffer.length + (buffer.length >> 1));
    }

}