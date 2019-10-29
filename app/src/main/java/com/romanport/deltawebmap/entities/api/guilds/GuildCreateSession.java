package com.romanport.deltawebmap.entities.api.guilds;

import java.io.Serializable;
import java.util.List;

public class GuildCreateSession implements Serializable {

    public Integer day;
    public Float dayTime;
    public String endpoint_dino_class_search;
    public String endpoint_game_map;
    public String endpoint_population_map;
    public String endpoint_tribes;
    public String endpoint_tribes_dino;
    public String endpoint_tribes_itemsearch;
    public String endpoint_tribes_overview;
    public String href;
    public String mapBackgroundColor;
    public GuildMapData mapData;
    public String mapName;
    public List<GuildDisplayMap> maps;

}
