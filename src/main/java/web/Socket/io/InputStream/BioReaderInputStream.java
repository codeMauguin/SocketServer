package web.Socket.io.InputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:41 下午 2021/12/5
 * @Modified By:
 */
public class BioReaderInputStream extends ReaderInputStream {

    private final InputStream stream;

    public BioReaderInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }
}
