package web.http.Filter;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:05 下午 2021/11/29
 * @Modified By:
 */
public class FilterRecord {
    private final String mapper;
    private final Filter filter;

    public FilterRecord(String mapper, Filter filter) {
        if (mapper.charAt(mapper.length() - 1) == '/') {
            mapper = mapper.concat(".*");
        } else {
            if (mapper.charAt(0) != '/') {
                mapper = "/".concat(mapper);
            }
        }
        this.mapper = mapper.replaceAll("\\*\\*", ".*");
        this.filter = filter;
    }

    public boolean matches(String path) {
        return mapper.matches(path);
    }

    public Filter getFilter() {
        return filter;
    }
}
