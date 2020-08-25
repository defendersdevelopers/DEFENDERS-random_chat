package com.kyle.random.deeparAiVedioCall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kyle.random.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.MyViewHolder> {

    private ArrayList<Filter> Filters;
    public Context context;
OnFilterChangeListener onFilterChangeListener;

    public FiltersAdapter(Context context, ArrayList<Filter> Filters, OnFilterChangeListener onFilterChangeListener) {
        this.Filters = Filters;
        this.context = context;
        this.onFilterChangeListener=onFilterChangeListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CircleImageView filterImageView;
        ProgressBar progressBar;
        FrameLayout item;
        CircleImageView appliedFrameLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.filterImageView = itemView.findViewById(R.id.filterImageView);
            this.appliedFrameLayout = itemView.findViewById(R.id.appliedFrameLayout);
            this.item = itemView.findViewById(R.id.item);
        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final CircleImageView filterImageView = holder.filterImageView;
        final CircleImageView appliedFrameLayout = holder.appliedFrameLayout;
        TextView name = holder.name;
        FrameLayout item = holder.item;

        final ProgressBar progressBar = holder.progressBar;

        final Filter filter = Filters.get(listPosition);
        if (filter.applied) {
            appliedFrameLayout.setVisibility(View.VISIBLE);

        }else {
            appliedFrameLayout.setVisibility(View.GONE);
        }

        name.setText(filter.name);


        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deselectAllFilters();
                filter.applied = true;
              notifyDataSetChanged();
                onFilterChangeListener.onFilterChange(filter.imageFilter);
            }
        });
        Picasso.get().load(filter.imageFilter).into(filterImageView);

    }

    public void deselectAllFilters() {
        for (Filter filter : Filters) {
            filter.applied = false;
        }
    }

    @Override
    public int getItemCount() {
        return Filters.size();
    }
}
