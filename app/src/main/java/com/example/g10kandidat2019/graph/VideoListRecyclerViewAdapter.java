package com.example.g10kandidat2019.graph;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.g10kandidat2019.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter used for RecycleView in VideoListActivity.
 */
public class VideoListRecyclerViewAdapter extends RecyclerView.Adapter<VideoListRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<String> dataList;
    private final List<Bitmap> thumbnails;

    /**
     * Creates Adapter object for the VideoListActivity
     * @param c Context
     * @param dataList List with path to video files to display
     */
    VideoListRecyclerViewAdapter(Context c, List<String> dataList) {
        this.context = c;
        this.dataList = dataList;
        thumbnails = new ArrayList<>();

        initLoadThumbnailsAsync();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.video_list_recycleview_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        if (thumbnails.size()-1 >= i) {
            View v = (viewHolder.view);
            String path = dataList.get(i).substring(dataList.get(i).lastIndexOf("/")+1);
            ((TextView)v.findViewById(R.id.video_list_item_descr)).setText(path);

            ((ImageView)v.findViewById(R.id.video_list_thumbnail)).setImageBitmap(thumbnails.get(i));

            // List items start as invisible due to the loading time of thumbnails
            viewHolder.view.findViewById(R.id.video_list_item_container).setVisibility(View.VISIBLE);
        }
        final int pos = i;
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GraphActivity.class);
                intent.putExtra("path", dataList.get(pos));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

         final View view;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }

    /**
     * Loads thumbnails for all videos into thumbnail list
     */
    private void loadThumbnails() {
        for (String s : dataList) {
            thumbnails.add(ThumbnailUtils.createVideoThumbnail(s,
                    MediaStore.Video.Thumbnails.MICRO_KIND));
        }
    }

    /**
     * Loads the thumbnails for all video with AsyncTask and updates content when finished
     */
    private void initLoadThumbnailsAsync() {
        AsyncLoadingTask loadingTask = new AsyncLoadingTask(context, new AsyncLoadingTask.TaskAction() {
            @Override
            public void onPreExecute() { }
            @Override
            public void doInBackground() {
                loadThumbnails();
            }

            @Override
            public void onPostExecute() {
                notifyDataSetChanged();
            }
        }, false);
        loadingTask.setLoadingMessage(context.getResources()
                .getString(R.string.videolist_loading_description));
        loadingTask.execute();
    }

}

