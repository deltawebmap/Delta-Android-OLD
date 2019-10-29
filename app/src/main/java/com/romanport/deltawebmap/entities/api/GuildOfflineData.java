package com.romanport.deltawebmap.entities.api;

import com.romanport.deltawebmap.entities.api.guilds.GuildCreateSession;
import com.romanport.deltawebmap.entities.api.overview.TribeOverviewPayload;
import com.romanport.deltawebmap.entities.api.tribes.TribeMapPayload;

import java.io.Serializable;

public class GuildOfflineData implements Serializable {

    public TribeOverviewPayload overview;
    public GuildCreateSession session;
    public TribeMapPayload tribe;

}
