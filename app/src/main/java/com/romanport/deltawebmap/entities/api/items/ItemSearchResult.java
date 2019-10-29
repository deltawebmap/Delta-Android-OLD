package com.romanport.deltawebmap.entities.api.items;

import java.io.Serializable;
import java.util.List;

public class ItemSearchResult implements Serializable {

    public String item_classname;
    public String item_displayname;
    public String item_icon;
    public List<ItemSearchResultInventory> owner_inventories;
    public Integer total_count;

}
