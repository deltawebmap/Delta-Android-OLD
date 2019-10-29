package com.romanport.deltawebmap.entities;

import java.io.Serializable;

public class DeltaVector3 implements Serializable {

    public Float x;
    public Float y;
    public Float z;

    public static DeltaVector3 Create(Float x, Float y, Float z) {
        DeltaVector3 t = new DeltaVector3();
        t.x = x;
        t.y = y;
        t.z = z;
        return t;
    }

}
