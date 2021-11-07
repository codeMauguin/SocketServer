package web.Socket;

import java.net.Socket;

public class HttpFactory extends WebHttpServerFactory {

    @Override
    public void start(Integer k) throws Throwable {
        super.start (k);
        /*
         *
         */
        while (start) {
            Socket accept = this.serverSocket.accept ( );
            executor.execute (new SockAccept (accept, filters, servletFactory));
        }
    }

    @Override
    public void destroy(Integer k) {
        super.destroy (k);
        /*
         *
         */
    }
}
