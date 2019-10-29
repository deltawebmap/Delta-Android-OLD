package com.romanport.deltawebmap.entities.api.tribes;

import com.romanport.deltawebmap.entities.DeltaVector2;

import java.io.Serializable;

public class TribeMapPlayer implements Serializable {

    public DeltaVector2 adjusted_map_pos;
    public DeltaVector2 coord_pos;
    public Boolean is_alive;
    public TribeMapPlayerArkProfile profile;
    public TribeMapPlayerSteamProfile steamProfile;

}
