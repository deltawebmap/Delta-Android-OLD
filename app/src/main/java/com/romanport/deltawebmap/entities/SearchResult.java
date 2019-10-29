package com.romanport.deltawebmap.entities;

import java.util.List;

public class SearchResult {

    public String img;
    public Boolean invertImg;
    public String title;
    public String subtitle;

    public Integer callbackId;
    public String callbackData;

    public List<SearchResult> children;

}
