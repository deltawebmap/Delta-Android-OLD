package com.romanport.deltawebmap.entities.api.dinosaur;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class ArkDinoPackage implements Serializable {

    public ArkDino dino;
    public ArkDinoEntry dino_entry;
    public DeltaDinoSettings dino_settings;
    public String href;
    public List<ArkDinoBaseInstance> inventory_items;
    public HashMap<String, ArkItemEntry> item_class_data;
    public ArkDinoLevelups max_stats;

}
