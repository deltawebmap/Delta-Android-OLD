package com.romanport.deltawebmap.entities.api.guilds;

import com.romanport.deltawebmap.entities.DeltaVector2;

import java.io.Serializable;

public class GuildMapData implements Serializable {

    public String backgroundColor;
    public GuildMapBounds bounds;
    public String displayName;
    public Boolean isOfficial;
    public Float latLongMultiplier;
    public DeltaVector2 mapImageOffset;
    public Float pixelsPerMeter;
    public Integer sourceImageSize;

}
