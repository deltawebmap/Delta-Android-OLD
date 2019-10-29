package com.romanport.deltawebmap.activites.main.dino;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.romanport.deltawebmap.HttpTool;
import com.romanport.deltawebmap.R;
import com.romanport.deltawebmap.activites.main.HqActivity;
import com.romanport.deltawebmap.entities.DeltaVector2;
import com.romanport.deltawebmap.entities.api.dinosaur.ArkDinoBaseInstance;
import com.romanport.deltawebmap.entities.api.dinosaur.ArkDinoPackage;
import com.romanport.deltawebmap.entities.api.dinosaur.ArkItemEntry;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     DinoDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link DinoDialogFragment.Listener}.</p>
 */
public class DinoDialogFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_DATA_ICON = "data-icon";
    private static final String ARG_DATA_NAME = "data-name";
    private static final String ARG_DATA_SPECIES = "data-species";
    private static final String ARG_API = "api-url";
    private static final String ARG_DATA_POS = "data-pos";
    private Listener mListener;

    // TODO: Customize parameters
    public static DinoDialogFragment newInstance(String api, DeltaVector2 posAdjusted, String name, String species, String icon_url) {
        final DinoDialogFragment fragment = new DinoDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_API, api);
        args.putString(ARG_DATA_ICON, icon_url);
        args.putString(ARG_DATA_NAME, name);
        args.putString(ARG_DATA_SPECIES, species);
        args.putSerializable(ARG_DATA_POS, posAdjusted);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dinoinventory_dialog, container, false);
    }

    public ArkDinoPackage dinoData;
    public DeltaVector2 dinoPosAdjusted;

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        //Get dino data
        String apiEndpoint = getArguments().getString(ARG_API);
        String name = getArguments().getString(ARG_DATA_NAME);
        String icon = getArguments().getString(ARG_DATA_ICON);
        String species = getArguments().getString(ARG_DATA_SPECIES);
        dinoPosAdjusted = (DeltaVector2)getArguments().getSerializable(ARG_DATA_POS);

        //Set up name
        HqActivity.AddWebImage(icon, (ImageView)view.findViewById(R.id.dinoImg), true);
        ((TextView)view.findViewById(R.id.dinoName)).setText(name);
        ((TextView)view.findViewById(R.id.dinoSub)).setText(species);

        //Request data
        HttpTool.SendGet(getContext(), apiEndpoint, ArkDinoPackage.class, new Response.Listener<ArkDinoPackage>() {
            @Override
            public void onResponse(ArkDinoPackage response) {
            Log.d("awm-hq-load", "Obtained dino data.");
            OnDinoDataDownloaded(view, response);
            }
        });
    }

    private void OnDinoDataDownloaded(View view, ArkDinoPackage data) {
        //Set up inventory, if it exists
        if(data.inventory_items.size() > 0) {
            //Set inventory to visible
            RecyclerView inventoryView = (RecyclerView) view.findViewById(R.id.inventory);
            inventoryView.setVisibility(View.VISIBLE);

            //Determine number of rows we can have to still fit inside of the screen
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            int rows = (int)Math.floor((displayMetrics.widthPixels / displayMetrics.density) / 100);

            //Add items
            inventoryView.setLayoutManager(new GridLayoutManager(getContext(), rows));
            inventoryView.setAdapter(new DinoInventoryAdapter(data));
        }

        //Add stats
        SetStatusField(view.findViewById(R.id.dinoStatHealth), R.string.stat_health, R.drawable.arkstatusicon_health, Math.round(data.max_stats.health), Math.round(data.dino.current_stats.health));
        SetStatusField(view.findViewById(R.id.dinoStatStamina), R.string.stat_stamina, R.drawable.arkstatusicon_stamina, Math.round(data.max_stats.stamina), Math.round(data.dino.current_stats.stamina));
        SetStatusField(view.findViewById(R.id.dinoStatWeight), R.string.stat_weight, R.drawable.arkstatusicon_inventoryweight, Math.round(data.max_stats.inventoryWeight), Math.round(data.dino.current_stats.inventoryWeight));
        SetStatusField(view.findViewById(R.id.dinoStatFood), R.string.stat_food, R.drawable.arkstatusicon_food, Math.round(data.max_stats.food), Math.round(data.dino.current_stats.food));
        view.findViewById(R.id.dinoStatsContainerUpper).setVisibility(View.VISIBLE);
        view.findViewById(R.id.dinoStatsContainerLower).setVisibility(View.VISIBLE);

        //Remove loader
        view.findViewById(R.id.loadingBar).setVisibility(View.GONE);
    }

    private static void SetStatusField(View parent, Integer nameStringId, Integer imageId, Integer max, Integer value) {
        ((ImageView)parent.findViewById(R.id.dino_stat_progressbar_image)).setImageResource(imageId);
        ((TextView)parent.findViewById(R.id.dino_stat_progressbar_name)).setText(nameStringId);
        ((TextView)parent.findViewById(R.id.dino_stat_progressbar_amount)).setText(value+"/"+max);
        ProgressBar b = (ProgressBar)parent.findViewById(R.id.dino_stat_progressbar);
        b.setMax(max);
        b.setProgress(value);
    }

    public void OnClickMore(View v) {
        final PopupMenu p = new PopupMenu(getContext(), v);
        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.dino_more_show_on_map:
                        mListener.GoToMapPos(dinoPosAdjusted);
                        dismiss();
                        break;
                }
                p.dismiss();
                return true;
            }
        });
        p.inflate(R.menu.dino_dialog_more);
        p.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (Listener) parent;
        } else {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public interface Listener {
        void GoToMapPos(DeltaVector2 pos);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView count;
        final TextView weight;
        final ImageView icon;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            // TODO: Customize the item layout
            super(inflater.inflate(R.layout.fragment_dinoinventory_dialog_inventory_item, parent, false));
            count = (TextView) itemView.findViewById(R.id.itemCount);
            weight = (TextView) itemView.findViewById(R.id.itemWeight);
            icon = (ImageView) itemView.findViewById(R.id.itemImage);
            count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        //mListener.onDinoInventoryClicked(getAdapterPosition());
                        //dismiss();
                    }
                }
            });
        }

    }

    private class DinoInventoryAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArkDinoPackage dino;

        DinoInventoryAdapter(ArkDinoPackage dino) {
            this.dino = dino;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ArkDinoBaseInstance r = dino.inventory_items.get(position);
            ArkItemEntry i = dino.item_class_data.get(r.classname);

            holder.count.setText("x"+String.valueOf(r.stack_size));

            if(i != null) {
                Float weight = ((float)Math.round((i.baseItemWeight * r.stack_size) * 10)) / 10;

                holder.weight.setText(String.valueOf(weight));
                HqActivity.AddWebImage(i.icon.image_url, holder.icon);
            } else {
                holder.weight.setText(R.string.error_missing_item_data);
                holder.icon.setImageResource(R.drawable.img_failed);
            }
        }

        @Override
        public int getItemCount() {
            return dino.inventory_items.size();
        }

    }

}
