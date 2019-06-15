package WalkTogether.com.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import WalkTogether.com.Photo;
import WalkTogether.com.R;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder> {

    private Context mContext ;
    private List<Photo> mData ;


    public RecycleViewAdapter(Context mContext, List<Photo> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.content_photoview_row,parent,false) ;

        Log.d("check_cameralist", "test");

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Log.d("check_cameralist", "test");
        myViewHolder.photo_date.setText(mData.get(i).getImageFileName());
        myViewHolder.photo_mood.setText(mData.get(i).getmood());

        Picasso.with(mContext)
                .load("https://firebasestorage.googleapis.com/v0/b/letswalk-c0e21.appspot.com/o/"+ mData.get(i).getImageFileName() +"?alt=media&token=0eb1f012-f558-40d8-bae1-af3ab61bef02")
                .into(myViewHolder.img_photo, new DownloadimgCallbak());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView photo_date, photo_mood;
        ImageView img_photo;

        public MyViewHolder(View itemView) {
            super(itemView);

            img_photo = itemView.findViewById(R.id.photo_image);
            photo_date = itemView.findViewById(R.id.photo_date);
            photo_mood = itemView.findViewById(R.id.photo_mood);

        }
    }

    private class DownloadimgCallbak implements Callback {


        @Override
        public void onSuccess() {

        }

        @Override
        public void onError() {

        }
    }

}

