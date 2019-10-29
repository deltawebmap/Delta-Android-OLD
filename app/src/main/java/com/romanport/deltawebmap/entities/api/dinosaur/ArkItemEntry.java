package com.romanport.deltawebmap.entities.api.dinosaur;

import com.romanport.deltawebmap.entities.DeltaIcon;

import java.io.Serializable;
import java.util.HashMap;

public class ArkItemEntry implements Serializable {

    public HashMap<String, ArkItemEntryAddStatusValue> addStatusValues;
    public Boolean allowUseWhileRiding;
    public Float baseCraftingXP;
    public Float baseItemWeight;
    public Float baseRepairingXP;
    public String blueprintPath;
    public DeltaIcon broken_icon;
    public String classname;
    public String description;
    public Boolean hideFromInventoryDisplay;
    public DeltaIcon icon;
    public Float increasePerQuanity_Food;
    public Float increasePerQuanity_Health;
    public Float increasePerQuanity_Stamina;
    public Float increasePerQuanity_Water;
    public Float increasePerQuanity_Weight;
    public Boolean isTekItem;
    public Integer maxItemQuantity;
    public String name;
    public Float spoilingTime;
    public Float useCooldownTime;
    public Boolean useItemDurability;

}
