package com.mscg.asf.impl.index;

import java.io.InputStream;

import com.mscg.asf.exception.InvalidObjectDataException;
import com.mscg.asf.guid.ASFObjectGUID;
import com.mscg.asf.impl.ASFGenericObject;

/**
 * This class maps an ASF index object.
 *
 * @author Giuseppe Miscione
 *
 */
public class ASFIndexObject extends ASFGenericObject {

    /**
     * Creates a new instance of an ASF index object.
     *
     * @param guid The object GUID.
     * @param length The length of the object, in bytes.
     * @param data The object data as an {@link InputStream}.
     * @throws InvalidObjectDataException If the object data are not valid.
     */
    public ASFIndexObject(ASFObjectGUID guid, long length, InputStream data) throws InvalidObjectDataException {
        super(guid, length, data);
    }

}
