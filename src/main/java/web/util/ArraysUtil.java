package web.util;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 8:54 AM 2021/12/25
 * @Modified By:
 */
public class ArraysUtil {
    public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.parallelStream().filter(predicate).findFirst().orElse(null);
    }
}
