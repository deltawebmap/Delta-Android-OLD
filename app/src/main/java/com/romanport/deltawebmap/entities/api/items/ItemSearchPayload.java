package com.romanport.deltawebmap.entities.api.items;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public class ItemSearchPayload implements Serializable {

    public List<ItemSearchResult> items;
    public Boolean more;
    public HashMap<String, HashMap<String, ItemSearchInventoryDino>> inventories;
    public Integer page_offset;
    public String query;
    public Integer total_item_count;

}
