package com.romanport.deltawebmap.activites.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.romanport.deltawebmap.HttpTool;
import com.romanport.deltawebmap.R;
import com.romanport.deltawebmap.entities.SearchResult;
import com.romanport.deltawebmap.entities.api.AppConfig;
import com.romanport.deltawebmap.entities.api.guilds.GuildCreateSession;
import com.romanport.deltawebmap.entities.api.items.ItemSearchInventoryDino;
import com.romanport.deltawebmap.entities.api.items.ItemSearchPayload;
import com.romanport.deltawebmap.entities.api.items.ItemSearchResult;
import com.romanport.deltawebmap.entities.api.items.ItemSearchResultInventory;
import com.romanport.deltawebmap.entities.api.overview.TribeOverviewDino;
import com.romanport.deltawebmap.entities.api.overview.TribeOverviewPayload;

import java.util.LinkedList;
import java.util.List;

public class HqTribeSearchView extends ConstraintLayout {

    public HqTribeSearchView(Context c, AttributeSet attrs) {
        super(c, attrs);
        isShown = false;
        isLoaded = false;
        isAnimationBusy = false;
        resultToken = 0;
        isLocked = true;
        queuedQuery = null;
    }

    public RecyclerView content;
    public EditText input;
    public ImageView oldBtn;
    public ImageView backBtn;
    public AppConfig config;
    public List<SearchResult> results;

    public void SetContent(RecyclerView content, EditText edit, ImageView oldBtn, ImageView backBtn, AppConfig config) {
        this.content = content;
        this.input = edit;
        this.oldBtn = oldBtn;
        this.backBtn = backBtn;
        this.config = config;
        results = new LinkedList<>();
        content.setLayoutManager(new LinearLayoutManager(getContext()));
        content.setAdapter(new ResultsAdapter(this, latestActivity));
    }

    public Boolean isShown;
    public Boolean isLoaded;
    public Boolean isAnimationBusy;
    public Integer resultToken;
    public Boolean isLocked;
    public String queuedQuery;

    public TribeOverviewPayload latestData;
    public GuildCreateSession latestSession;
    public HqActivity latestActivity;

    public void OnQueryChanged(String s, TribeOverviewPayload data, GuildCreateSession session, final HqActivity activity) {
        //Update refs
        latestData = data;
        latestSession = session;
        latestActivity = activity;

        //Check if we're locked
        /*if(isLocked) {
            queuedQuery = s;
            return;
        }*/

        //Update
        resultToken += 1;
        DoSearch(s.toLowerCase(), resultToken, data, session, activity);
    }

    public void DoCollapse() {
        //Grab the animation duration
        int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        //Fade out
        animate().alpha(0f).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                isShown = false;
            }
        });

        //Deactivate text
        input.clearFocus();

        //Hide virtual keyboard (gross)
        InputMethodManager imm = (InputMethodManager)input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

        //Clear
        input.setText("");

        //Fade out back button
        backBtn.setVisibility(GONE);
        backBtn.animate().alpha(0f).setDuration(shortAnimationDuration);

        //Fade in old icon
        oldBtn.setVisibility(VISIBLE);
        oldBtn.animate().alpha(1f).setDuration(shortAnimationDuration);
    }

    public void DoShow() {
        //Grab the animation duration
        int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        //Fade in
        setVisibility(VISIBLE);
        animate().alpha(1f).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isShown = true;
            }
        });

        //Activate text
        input.requestFocus();

        //Fade in back button
        backBtn.setVisibility(VISIBLE);
        backBtn.animate().alpha(1f).setDuration(shortAnimationDuration);

        //Fade out old icon
        oldBtn.setVisibility(GONE);
        oldBtn.animate().alpha(0f).setDuration(shortAnimationDuration);
    }

    public void DoUnlock() {
        if(isLocked) {
            isLocked = false;
            if(queuedQuery != null) {
                OnQueryChanged(queuedQuery, latestData, latestSession, latestActivity);
                queuedQuery = null;
            }
        }
    }

    public void PutNewResults(List<SearchResult> r, final HqActivity activity) {
        results = r;
        content.setAdapter(new ResultsAdapter(this, activity));
        //content.getAdapter().notifyDataSetChanged();
    }

    public void DoSearch(final String query, final Integer token, TribeOverviewPayload dataset, GuildCreateSession session, final HqActivity activity) {
        //Build results
        List<SearchResult> rs = new LinkedList<>();
        for(TribeOverviewDino d : dataset.dinos) {
            if(d.displayName.toLowerCase().contains(query) || d.classDisplayName.toLowerCase().contains(query)) {
                SearchResult dr = new SearchResult();
                dr.callbackData = d.id;
                dr.callbackId = 0;
                dr.children = new LinkedList<>();
                dr.img = d.img;
                dr.subtitle = d.classDisplayName;
                dr.title = d.displayName;
                dr.invertImg = true;
                rs.add(dr);
            }
        }

        //Update
        PutNewResults(rs, activity);

        //Now, fetch items ItemSearchPayload
        final HqTribeSearchView context = this;
        if(config.IsFeatureEnabled("ARK_SEARCH_TRIBE_INVENTORY")) {
            HttpTool.SendGet(getContext(), session.endpoint_tribes_itemsearch.replace("{query}", query), ItemSearchPayload.class, new Response.Listener<ItemSearchPayload>() {
                @Override
                public void onResponse(ItemSearchPayload response) {
                    //Check if we're still valid
                    if(!token.equals(resultToken)) {
                        //Abort
                        return;
                    }

                    //Lock
                    isLocked = true;

                    //Create data
                    List<SearchResult> rs = new LinkedList<>();
                    for(ItemSearchResult result : response.items) {
                        SearchResult r = new SearchResult();
                        r.callbackData = "";
                        r.callbackId = -1;
                        r.children = new LinkedList<>();
                        r.img = result.item_icon;
                        r.subtitle = "x"+result.total_count;
                        r.title = result.item_displayname;
                        r.invertImg = false;
                        for(ItemSearchResultInventory inventory : result.owner_inventories) {
                            SearchResult ir = new SearchResult();
                            ItemSearchInventoryDino dino = response.inventories.get(inventory.type.toString()).get(inventory.id);
                            ir.img = dino.img;
                            ir.invertImg = true;
                            ir.callbackId = 0;
                            ir.callbackData = dino.id;
                            ir.title = dino.displayName + " (x"+inventory.count+")";
                            r.children.add(ir);
                        }
                        rs.add(r);
                    }

                    //Now, add it and refresh
                    results.addAll(rs);
                    content.setAdapter(new ResultsAdapter(context, activity));

                    DoUnlock();
                }
            });
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView sub;
        final ImageView image;
        final ListView children;
        final View parentView;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.tribe_search_item, parent, false));
            name = (TextView) itemView.findViewById(R.id.resultTitle);
            sub = (TextView) itemView.findViewById(R.id.resultSub);
            image = (ImageView) itemView.findViewById(R.id.resultImage);
            children = (ListView) itemView.findViewById(R.id.resultChildren);
            parentView = itemView;
        }

    }

    private class ResultsAdapter extends RecyclerView.Adapter<HqTribeSearchView.ViewHolder> {

        HqTribeSearchView context;
        HqActivity activity;

        ResultsAdapter(HqTribeSearchView context, HqActivity activity) {
            this.context = context;
            this.activity = activity;
            Integer i = context.results.size();
        }

        @Override
        public HqTribeSearchView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new HqTribeSearchView.ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final HqTribeSearchView.ViewHolder holder, int position) {
            final SearchResult r = context.results.get(position);
            HqActivity.AddWebImage(r.img, holder.image, r.invertImg);
            holder.name.setText(r.title);
            holder.sub.setText(r.subtitle);
            TribeSearchListItemEntryAdapter childAdapter = new TribeSearchListItemEntryAdapter(getContext(), r.children);
            holder.children.setAdapter(childAdapter);

            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(r.callbackId == 0) {
                        activity.MapOnDinoClicked(latestSession.endpoint_tribes_dino.replace("{dino}", r.callbackData));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            Integer i = context.results.size();
            Log.d("awm-debug", i.toString());
            return context.results.size();
        }

    }

    public class TribeSearchListItemEntryAdapter extends ArrayAdapter<SearchResult> {

        private final Context context;
        private final List<SearchResult> results;

        public TribeSearchListItemEntryAdapter(Context context, List<SearchResult> results) {
            super(context, R.layout.tribe_search_item_mini, results);
            this.context=context;
            this.results=results;
        }

        public View getView(int position, View view, ViewGroup parent) {
            View rowView = LayoutInflater.from(context).inflate(R.layout.tribe_search_item_mini, null,true);
            TextView name = (TextView) rowView.findViewById(R.id.resultTitle);
            ImageView image = (ImageView) rowView.findViewById(R.id.resultImage);

            SearchResult data = results.get(position);
            name.setText(data.title);
            HqActivity.AddWebImage(data.img, image, data.invertImg);

            return rowView;

        }
    }
}
