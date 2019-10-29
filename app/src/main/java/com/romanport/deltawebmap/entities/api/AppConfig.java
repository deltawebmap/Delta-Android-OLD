package com.romanport.deltawebmap.entities.api;

import java.io.Serializable;
import java.util.HashMap;

public class AppConfig implements Serializable {

    public String enviornment;
    public String api;
    public String[] disabled_features;
    public HashMap<String, String> oldest_versions;
    public Boolean status;
    public String status_message;
    public Integer refresh_policy_seconds;
    public Integer terms_version;
    public Integer config_version;
    public HashMap<String, String> urls;
    public String[] allowed_domains;

}
