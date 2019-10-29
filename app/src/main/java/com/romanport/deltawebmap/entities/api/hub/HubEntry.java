package com.romanport.deltawebmap.entities.api.hub;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class HubEntry implements Serializable {

    public HashMap<String, HubTargetContainer> targets;
    public Integer type;
    public Integer priority;
    public String gameDay;
    public String gameTime;
    public String serverId;
    public Integer tribeId;
    public Date time;

}
