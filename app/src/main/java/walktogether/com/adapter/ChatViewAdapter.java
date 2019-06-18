package walktogether.com.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import walktogether.com.R;
import walktogether.com.chatline;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class ChatViewAdapter extends RecyclerView.Adapter<ChatViewAdapter.MyViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context chatline_mContext ;
    private List<chatline> chatline_mData ;
    private String id;

    public ChatViewAdapter(Context mContext, List<chatline> mData) {
        this.chatline_mContext = mContext;
        this.chatline_mData = mData;
        this.id = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater inflater = LayoutInflater.from(chatline_mContext);
        if(viewType == VIEW_TYPE_MESSAGE_SENT){
            view = inflater.inflate(R.layout.content_chatroom_my,parent,false) ;
        }else{
            view = inflater.inflate(R.layout.content_chatroom_other,parent,false) ;
        }

        return new MyViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {

        if(chatline_mData.get(position).getpeople().equals(id))
        {
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else{
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.message.setText(chatline_mData.get(i).getsentence());
        if(chatline_mData.get(i).getpeople().equals(id))
            myViewHolder.head.setImageResource(R.drawable.boy_round);
        else
            myViewHolder.head.setImageResource(R.drawable.girl_round);

    }

    @Override
    public int getItemCount() {
        return chatline_mData.size();
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView head;
        TextView message;
        ConstraintLayout chatbox;


        MyViewHolder(View itemView) {
            super(itemView);

            chatbox = itemView.findViewById(R.id.chatbox);
            head = itemView.findViewById(R.id.messagehead);
            message = itemView.findViewById(R.id.txtMessage);

        }
    }
}