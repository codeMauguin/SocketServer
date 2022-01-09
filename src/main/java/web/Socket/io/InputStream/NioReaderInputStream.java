package web.Socket.io.InputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:04 下午 2021/12/5
 * @Modified By:
 */
public class NioReaderInputStream extends ReaderInputStream {
    private final SocketChannel client;

    public NioReaderInputStream(SocketChannel client) {
        this.client = client;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate(1);
        int read = client.read(allocate);
        allocate.flip();
        return (read == -1 || read == 0) ? -1 : allocate.get();
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
        b[off] = (byte) (c & 0xff);
        int i = 1;
        ByteBuffer allocate = ByteBuffer.allocate(len - 1);
        int read = client.read(allocate);
        if (read == -1) {
            return i;
        } else {
            i = read + 1;
        }
        System.arraycopy(allocate.array(), 0, b, off + 1, allocate.position());
        return i;
    }

}
