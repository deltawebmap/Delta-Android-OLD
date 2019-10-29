package com.romanport.deltawebmap.entities.api.overview;

import java.io.Serializable;
import java.util.List;

public class TribeOverviewPayload implements Serializable {

    //baby dinos
    public List<TribeOverviewDino> dinos;
    public String tribeName;
    public List<TribeOverviewPlayer> tribemates;

}
