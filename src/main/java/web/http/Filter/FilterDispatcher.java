package web.http.Filter;

import web.Socket.Handle.HttpHandle;
import web.http.HttpRequest;
import web.http.HttpResponse;
import web.server.WebServerContext;

import java.util.List;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 11:20 AM 2022/1/9
 * @Modified By:
 */
public class FilterDispatcher implements FilterChain {
    private final WebServerContext context;
    private final HttpHandle handle;
    private int filterIndex = 0;

    public FilterDispatcher(WebServerContext context, HttpHandle handle) {
        this.context = context;
        this.handle = handle;
    }

    public void release() {
        filterIndex = 0;
    }


    @Override
    public void doFilter(HttpRequest request, HttpResponse response) {
        List<FilterRecord> filter = context.getFilter(request.getPath());
        if (filterIndex == filter.size()) {
            handle.service(request, response);
        } else {
            filter.get(filterIndex++).filter().doFilter(request, response, this::doFilter);
        }
    }

}
