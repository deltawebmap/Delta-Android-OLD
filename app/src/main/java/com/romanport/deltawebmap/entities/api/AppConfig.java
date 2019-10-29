package com.romanport.deltawebmap.entities.api;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;

public class AppConfig implements Serializable {

    public String enviornment;
    public String api;
    public String[] disabled_features;
    public HashMap<String, Integer> oldest_versions;
    public Boolean status;
    public String status_message;
    public Integer refresh_policy_seconds;
    public Integer terms_version;
    public Integer config_version;
    public HashMap<String, String> urls;
    public String[] allowed_domains;

    //ARK_SEARCH_TRIBE_INVENTORY
    public Boolean IsFeatureEnabled(String name) {
        for(int i = 0; i<disabled_features.length; i++) {
            if(disabled_features[i].equals(name)) {
                Log.d("dwm-disabled-feature", "Remote config disabled feature "+name+"; it was just attempted to be used.");
                return false;
            }
        }
        return true;
    }

}
