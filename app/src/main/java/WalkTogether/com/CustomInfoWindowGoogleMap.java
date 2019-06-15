package WalkTogether.com;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.custom_info_window, null);

        ImageView img = view.findViewById(R.id.map_img);
        TextView content = view.findViewById(R.id.map_info);

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        Picasso.with(context)
                .load("https://firebasestorage.googleapis.com/v0/b/letswalk-c0e21.appspot.com/o/"+ infoWindowData.getImage() +"?alt=media&token=0eb1f012-f558-40d8-bae1-af3ab61bef02")
                .into(img, new DownloadimgCallbak(marker));

        content.setText(infoWindowData.getContent());


        return view;
    }

    private class DownloadimgCallbak implements Callback {

        Marker marker;

        DownloadimgCallbak(Marker marker){
            this.marker = marker;
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }

        @Override
        public void onError() {

        }
    }
}
