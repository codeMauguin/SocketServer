package web.http.Filter;

import java.util.regex.Pattern;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 2:05 下午 2021/11/29
 * @Modified By:
 */
public record FilterRecord(String mapper, Filter filter, Integer index) implements Comparable<FilterRecord> {

    public FilterRecord(String mapper, Filter filter, Integer index) {
        if (mapper.charAt(mapper.length() - 1) == '/') {
            mapper = mapper.concat(".*");
        } else {
            if (mapper.charAt(0) != '/') {
                mapper = "/".concat(mapper);
            }
        }
        this.mapper = mapper.replaceAll("\\*\\*", ".*");
        this.filter = filter;
        this.index = index;
    }

    public boolean matches(String path) {
        return Pattern.matches(mapper, path);
    }

    @Override
    public int compareTo(FilterRecord o) {
        return (index < o.index) ? -1 : 1;
    }
}
