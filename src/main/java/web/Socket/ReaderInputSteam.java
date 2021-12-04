package web.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 4:31 下午 2021/12/4
 * @Modified By:
 */
public class ReaderInputSteam extends InputStream {
    private final InputStream inputStream;

    /*

     */
    private int state = 0;
    private int[] buffer = new int[0];

    private int writeIndex = 0;

    private int readlimit = 0;

    private int readIndex = 0;

    public ReaderInputSteam(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private void refresh() {
        state = 0;
        buffer = new int[0];

        writeIndex = 0;

        readlimit = 0;

        readIndex = 0;

    }

    @Override
    public int read() throws IOException {
        /*判断是否在mark状态 在：返回并填充缓冲区，超过缓冲区长度
                 在reset 返回buffer index++


         */
        switch (state) {
            case 0 -> {
                return inputStream.read();
            }
            case 1 -> {
                if (writeIndex + 1 > readlimit) {
                    refresh();
                    return read();
                }
                int read = inputStream.read();
                buffer[writeIndex++] = read;
                return read;
            }
            case 3 -> {
                if (readIndex + 1 > readlimit) {
                    refresh();
                    return read();
                }
                return buffer[readIndex++];
            }
        }
        return inputStream.read();
    }

    public void next(int var0) throws IOException {
        int var1 = inputStream.read();
        assert var1 == var0;
    }

    public void next() {
        if (state == 3) {
            readIndex++;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        if (readlimit > 0) {
            refresh();
            this.readlimit = readlimit;
            this.state = 1;
            this.buffer = new int[readlimit];
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }
        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte) c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte) c;
            }
        } catch (IOException ignore) {
        }
        return i;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (state != 1) {
            throw new IOException("Resetting to invalid mark");
        }
        this.state = 3;
        this.readIndex = 0;
    }

    @Override
    public boolean markSupported() {
        return true;
    }
}
