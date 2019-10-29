package com.romanport.deltawebmap.entities.api;

import java.io.Serializable;
import java.util.List;

public class DeltaUser implements Serializable {

    public String id;
    public String profile_image_url;
    public String screen_name;
    public List<DeltaUserServer> servers;
    public String steam_id;
    public DeltaUserSettings user_settings;

}
