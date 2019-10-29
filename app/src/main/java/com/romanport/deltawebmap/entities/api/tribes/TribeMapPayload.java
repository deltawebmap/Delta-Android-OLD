package com.romanport.deltawebmap.entities.api.tribes;

import java.io.Serializable;
import java.util.List;

public class TribeMapPayload implements Serializable {

    //baby dinos
    public List<TribeMapDino> dinos;
    public Float gameTime;
    public List<TribeMapPlayer> player_characters;
    public List<TribeMapStructure> structures;
    public Integer tribeId;

}
