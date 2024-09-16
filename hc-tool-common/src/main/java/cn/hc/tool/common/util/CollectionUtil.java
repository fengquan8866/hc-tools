package cn.hc.tool.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cdhuangchao3
 * @date 2022/5/22 4:17 PM
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtil {

    /**
     * 是否为空
     */
    public static boolean isEmpty(Collection<?> col) {
        return col == null || col.isEmpty();
    }

    /**
     * source中包含任一候选
     *
     * @param source     源
     * @param candidates 候选
     */
    public static boolean containsAny(Collection<?> source, Collection<?> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return false;
        }
        for (Object c : candidates) {
            if (source.contains(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 集合截取
     *
     * @param list  原list
     * @param start 起始位置
     * @param end   截止位置
     * @param <E>   集合元素类型
     * @return 截取后的集合
     */
    public static <E> List<E> subList(List<E> list, int start, int end) {
        if (isEmpty(list)) {
            return list;
        }
        int size = list.size();
        if (size <= start) {
            return new ArrayList<E>();
        }
        return list.subList(start, Math.min(size, end));
    }
}
