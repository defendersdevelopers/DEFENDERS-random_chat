package com.kyle.random.deeparAiVedioCall;
import android.app.Activity;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kyle.random.R;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
public class AIAdapter extends RecyclerView.Adapter<AIAdapter.MyViewHolder> {
    private ArrayList<AIFace> AIFaces;
    public Activity context;
    OnAIFaceChangeListener onAIFaceChangeListener;
    public AIAdapter(Activity context, ArrayList<AIFace> AIFaces, OnAIFaceChangeListener onAIFaceChangeListener) {
        this.AIFaces = AIFaces;
        this.context = context;
        this.onAIFaceChangeListener = onAIFaceChangeListener;
        for (AIFace face : AIFaces) {
            if (face.applied) {
                setFilter(face, null);
            }
        }
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CircleImageView AIFaceImageView;
        ProgressBar progressBar;
        FrameLayout item;
        CircleImageView appliedFrameLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.AIFaceImageView = itemView.findViewById(R.id.filterImageView);
            this.appliedFrameLayout = itemView.findViewById(R.id.appliedFrameLayout);
            this.item = itemView.findViewById(R.id.item);
            this.progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }
    public void setFilter(final AIFace AIFace, final ProgressBar progressBar) {

        if (AIFace.path.startsWith("http")) {
            //todo show
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isLoading = true;
                    try {
                        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + ".filters");
                        if (!dir.isDirectory() || !dir.exists()) {
                            dir.mkdirs();
                        }
                        final File file = new File(dir, AIFace.name);
                        if (!file.exists()) {
                            FileUtils.copyURLToFile(new URL(AIFace.path), file);
                        }
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isLoading = false;
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                deselectAllAIFaces();
                                AIFace.applied = true;
                                notifyDataSetChanged();
                                onAIFaceChangeListener.onAIFaceChange(file.getAbsolutePath());
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            deselectAllAIFaces();
            AIFace.applied = true;
            notifyDataSetChanged();
            onAIFaceChangeListener.onAIFaceChange(AIFace.path);
        }
    }
    boolean isLoading = false;
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        final CircleImageView AIFaceImageView = holder.AIFaceImageView;
        final CircleImageView appliedFrameLayout = holder.appliedFrameLayout;
        TextView name = holder.name;
        FrameLayout item = holder.item;
        final ProgressBar progressBar = holder.progressBar;
        final AIFace AIFace = AIFaces.get(listPosition);
        if (AIFace.applied) {
            appliedFrameLayout.setVisibility(View.VISIBLE);
        } else {
            appliedFrameLayout.setVisibility(View.GONE);
        }
        name.setText(AIFace.name);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AIFace.path == null) {
                    deselectAllAIFaces();
                    AIFace.applied = true;
                    notifyDataSetChanged();
                    onAIFaceChangeListener.onAIFaceChange(null);
                } else {
                    setFilter(AIFace, progressBar);
                }
            }
        });
        Picasso.get().load(AIFace.icon).into(AIFaceImageView);
    }
    public void deselectAllAIFaces() {
        for (AIFace AIFace : AIFaces) {
            AIFace.applied = false;
        }
    }
    @Override
    public int getItemCount() {
        return AIFaces.size();
    }
}
