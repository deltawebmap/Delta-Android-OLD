package com.romanport.deltawebmap.entities.api.dinosaur;

import com.romanport.deltawebmap.entities.DeltaIcon;

import java.io.Serializable;
import java.util.List;

public class ArkDinoEntry implements Serializable {

    public float[] additiveTamingBonus;
    public List<ArkDinoEntryFood> adultFoods;
    public Float babyAgeSpeed;
    public Float babyGestationSpeed;
    public float[] baseLevel;
    public String blueprintPath;
    public List<ArkDinoEntryFood> childFoods;
    public String classname;
    public Float colorizationIntensity;
    public Float extraBabyAgeSpeedMultiplier;
    public Float extraBabyGestationSpeedMultiplier;
    public DeltaIcon icon;
    public float[] increasePerTamedLevel;
    public float[] increasePerWildLevel;
    public float[] multiplicativeTamingBonus;
    public String screen_name;
    public ArkDinoEntryStatusComponent statusComponent;
    public String thumb_icon_url;
    public Boolean useBabyGestation;

}
