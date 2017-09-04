package com.polar.browser.vclibrary.util;

import java.util.Collection;

/**
 * Created by James on 2016/9/18.
 */

public class Util {
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static boolean isCollectionEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }
}
