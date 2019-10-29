package com.romanport.deltawebmap.activites.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.romanport.deltawebmap.R;
import com.romanport.deltawebmap.entities.api.DeltaUser;
import com.romanport.deltawebmap.entities.api.DeltaUserServer;

import java.util.List;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     GuildListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link GuildListDialogFragment.Listener}.</p>
 */
public class GuildListDialogFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEMS = "user";
    private Listener mListener;

    // TODO: Customize parameters
    public static GuildListDialogFragment newInstance(DeltaUser user) {
        final GuildListDialogFragment fragment = new GuildListDialogFragment();
        final Bundle args = new Bundle();
        args.putSerializable(ARG_ITEMS, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guild_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DeltaUser user = (DeltaUser) getArguments().getSerializable(ARG_ITEMS);
        recyclerView.setAdapter(new GuildAdapter(user));
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
        void OnGuildClicked(int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final ImageView image;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            // TODO: Customize the item layout
            super(inflater.inflate(R.layout.fragment_guild_list_dialog_item, parent, false));
            name = (TextView) itemView.findViewById(R.id.guildText);
            image = (ImageView) itemView.findViewById(R.id.guildImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.OnGuildClicked(getAdapterPosition());
                        dismiss();
                    }
                }
            });
        }

    }

    private class GuildAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final List<DeltaUserServer> servers;

        GuildAdapter(DeltaUser user) {
            this.servers = user.servers;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DeltaUserServer guild = servers.get(position);
            holder.name.setText(guild.display_name);
            HqActivity.AddWebImage(guild.image_url, holder.image);
        }

        @Override
        public int getItemCount() {
            return servers.size();
        }

    }

}
