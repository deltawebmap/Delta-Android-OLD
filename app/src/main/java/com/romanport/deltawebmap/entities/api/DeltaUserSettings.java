package com.romanport.deltawebmap.entities.api;

import java.io.Serializable;
import java.util.List;

public class DeltaUserSettings implements Serializable {

    public List<String> custom_vulgar_words;
    public Boolean vulgar_filter_on;
    public Boolean vulgar_show_censored_on;

}
