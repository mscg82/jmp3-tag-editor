package com.mscg.jmp3.transformator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

public class StringTransformatorFactory {

    private static List<Class<? extends StringTransformator>> list;

    static {
        list = new LinkedList<>();

        Map<Integer, Class<? extends StringTransformator>> transformators =
            new TreeMap<>();
        ServiceLoader<StringTransformatorProvider> serviceLoader = ServiceLoader.load(StringTransformatorProvider.class);
        for(StringTransformatorProvider provider : serviceLoader) {
            transformators.putAll(provider.getStringTransformators());
        }

        // transformators are ordered against the integer key of the map
        list.addAll(transformators.values());
    }

    public static List<Class<? extends StringTransformator>> getStringTransformators() {
        return list;
    }

}
