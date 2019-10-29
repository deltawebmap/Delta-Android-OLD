package com.romanport.deltawebmap.entities.api.tribes;

import com.romanport.deltawebmap.entities.DeltaVector2;

import java.io.Serializable;

public class TribeMapStructure implements Serializable {

    public String apiUrl;
    public Boolean hasInventory;
    public String imgUrl;
    public DeltaVector2 map_pos;
    public Integer ppm;
    public Integer priority;
    public Float rot;

}
