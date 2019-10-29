package com.romanport.deltawebmap.entities.api;

import java.io.Serializable;

public class DeltaUserServer implements Serializable {

    public String arkName;
    public String display_name;
    public String endpoint_createsession;
    public String endpoint_offline_data;
    public String endpoint_hub;
    public Boolean has_ever_gone_online;
    public String id;
    public String image_url;
    public Boolean is_hidden;
    public Boolean is_public;
    //date
    public String map_id;
    public String map_name;
    public String owner_uid;
    //public listing
    public Integer tribeId;
    public String tribeName;

}
