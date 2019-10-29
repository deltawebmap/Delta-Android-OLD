package com.romanport.deltawebmap.entities.api.dinosaur;

import com.romanport.deltawebmap.entities.DeltaLocation;

import java.io.Serializable;
import java.util.List;

public class ArkDino implements Serializable {

    public Float baby_age;
    public Integer base_level;
    public ArkDinoLevelups base_levelups_applied;
    public String classname;
    public List<String> colors;
    public ArkDinoLevelups current_stats;
    public Float experience;
    public Float imprint_quality;
    public Boolean is_baby;
    public Boolean is_female;
    public Boolean is_tamed;
    public Integer level;
    public DeltaLocation location;
    public Float next_imprint_time;
    public ArkDinoLevelups tamed_levelups_applied;
    public String tamed_name;
    public String tamer_name;
    public Integer tribeId;

}
