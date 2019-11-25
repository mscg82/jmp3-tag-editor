package com.mscg.asf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.mscg.asf.guid.ASFObjectGUID;
import com.mscg.asf.impl.ASFGenericObject;

public class ASFObjectFactory {

    private static Map<ASFObjectGUID, Class<? extends ASFObject>> objects;

    static {
        objects = new LinkedHashMap<>();
        ServiceLoader<ASFObjectProvider> serviceLoader = ServiceLoader.load(ASFObjectProvider.class);
        for(ASFObjectProvider provider : serviceLoader) {
            objects.putAll(provider.getGUIDsToObjects());
        }
    }

    public static Class<? extends ASFObject> getObjectClassForGUID(ASFObjectGUID guid) {
        if(objects.containsKey(guid))
            return objects.get(guid);
        else
            return ASFGenericObject.class;
    }

}
