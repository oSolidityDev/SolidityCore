package io.github.abdullahcxd.soliditycore.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListUtils {

    public interface ListMapPredicate<V, R> {
        R call(V value, int index);
    }

    public static <V, R> @NotNull List<R> map(@NotNull List<V> list, ListMapPredicate<V, R> predicate) {
        List<R> rList = ChainedList.startChain();

        for (int index = 0; index<list.size(); index++) {
            V element = list.get(index);
            rList.add(predicate.call(element, index));
        }

        return rList;
    }
}
