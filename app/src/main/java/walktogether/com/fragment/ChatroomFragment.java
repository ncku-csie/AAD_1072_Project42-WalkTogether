package walktogether.com.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import walktogether.com.R;
import walktogether.com.adapter.ChatViewAdapter;
import walktogether.com.chatline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatroomFragment extends Fragment {

    private Button sendmessage_button;
    private EditText message;
    private RecyclerView chatroom_recyclerView;
    private List<chatline> listchatline;
    private ChatViewAdapter MY_ChatViewAdapter;
    private String uid;
    private String room_key;
    private DatabaseReference mdatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_chatroom, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendmessage_button = getView().findViewById(R.id.sendmessage);
        message = getView().findViewById(R.id.message);
        chatroom_recyclerView =  getView().findViewById(R.id.chat_recyclerView);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Log.d("check_chatroom", mAuth.getUid());
        uid = mAuth.getUid();



        listchatline = new ArrayList<>();



        message.setText("");

        GetRoomKey();


        chatroom_recyclerView.setHasFixedSize(true);
        chatroom_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sendmessage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!String.valueOf(message.getText()).equals("")) {
                    mdatabase = FirebaseDatabase.getInstance().getReference();
                    mdatabase.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("check_chatroom", String.valueOf(dataSnapshot.child("match").getValue()));
                            String room_key = String.valueOf(dataSnapshot.child("match").getValue());
                            String time = new SimpleDateFormat("yyyyMMdd_HHmmsss", Locale.getDefault()).format(new Date());

                            Map<String, Object> MessageDetial = new HashMap<>();
                            MessageDetial.put("people",uid);
                            MessageDetial.put("sentence",String.valueOf(message.getText()));

                            mdatabase.child("Room").child(room_key).child("message").child(time).updateChildren(MessageDetial);
                            message.setText("");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }
    private void GetRoomKey(){
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mdatabase.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("check_chatroom", String.valueOf(dataSnapshot.child("match").getValue()));
                room_key = String.valueOf(dataSnapshot.child("match").getValue());
                download_chatline_view();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void download_chatline_view(){
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mdatabase.child("Room").child(room_key).child("message").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("check_chatroom", String.valueOf(dataSnapshot));
                listchatline.add(new chatline(String.valueOf(dataSnapshot.child("people").getValue()), String.valueOf(dataSnapshot.child("sentence").getValue())));
                MY_ChatViewAdapter = new ChatViewAdapter(getActivity(), listchatline);
                chatroom_recyclerView.setAdapter(MY_ChatViewAdapter);
                chatroom_recyclerView.scrollToPosition(listchatline.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
