package WalkTogether.com.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import WalkTogether.com.R;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class WalkFragment extends Fragment {

    private TextView self_name, self_step, partner_name, partner_step, total_step;

    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    private DatabaseReference mdatabase;
    private String room_key, now, partner_id;
    private PieChartView pieChartView;
    private ArrayList<SliceValue> pieData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_walk, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        self_name = getActivity().findViewById(R.id.self_name);
        self_step = getActivity().findViewById(R.id.self_step);
        partner_name = getActivity().findViewById(R.id.partner_name);
        partner_step = getActivity().findViewById(R.id.partner_step);
        total_step = getActivity().findViewById(R.id.total_step);
        pieChartView = getActivity().findViewById(R.id.chart);

        pieData = new ArrayList<>();

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        now = ft.format(date);
        Log.d("check_WalkFragment", now + "");


        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();
        Log.d("check_WalkFragment", mAuth.getUid());



        if(mAuth!=null){
            mdatabase = FirebaseDatabase.getInstance().getReference();
            mdatabase.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("check_WalkFragment", String.valueOf(dataSnapshot.child("match").getValue()));
                    room_key = String.valueOf(dataSnapshot.child("match").getValue());
                    partner_id = String.valueOf(dataSnapshot.child("partner").getValue());
                    Log.d("check_WalkFragment", String.valueOf(dataSnapshot.child("match").getValue()));
                    mdatabase.child("Room").child(room_key).child("date").child(now).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("check_WalkFragment", String.valueOf(dataSnapshot));
                            self_step.setText(String.valueOf(dataSnapshot.child(mAuth.getUid()).getValue()));
                            partner_step.setText(String.valueOf(dataSnapshot.child(partner_id).getValue()));
                            total_step.setText(String.valueOf(dataSnapshot.child("TotalStep").getValue()));
                            int current_step = Integer.parseInt(String.valueOf(self_step.getText())) + Integer.parseInt(String.valueOf(partner_step.getText()));
                            int aim = Integer.parseInt(String.valueOf(total_step.getText()));
                            if((String.valueOf(dataSnapshot.child("state").getValue()).equals("0")) && (current_step >= aim)){
                                Log.d("check_WalkFragment", "達標");
                                Accomplish().addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("check_WalkFragment", String.valueOf(task.getResult()));
                                            if( String.valueOf(task.getResult()).equals("ok")){
                                                Toast.makeText(getContext(), "達成目標！", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }


                            pieData.clear();
                            pieData.add(new SliceValue(current_step, R.color.chart_finish));
                            pieData.add(new SliceValue(aim-current_step, Color.WHITE));
                            PieChartData pieChartData = new PieChartData(pieData);

                            pieChartData.setHasCenterCircle(true);
                            pieChartData.setCenterText1("Total Steps");
                            pieChartData.setCenterText1FontSize(20);
                            pieChartData.setCenterText2(current_step + "\n" + (int)((((float)current_step/(float)aim)*100)) + "%");
                            pieChartData.setCenterText2FontSize(20);
                            pieChartData.setCenterText1Color(Color.WHITE);
                            pieChartData.setCenterText2Color(Color.WHITE);
                            pieChartView.setPieChartData(pieChartData);
                            pieChartView.setChartRotation(270,true);
                            pieChartView.setViewportCalculationEnabled(true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private Task<String> Accomplish() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("key", room_key);

        return mFunctions
                .getHttpsCallable("Accomplish")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;

                    }
                });
    }
}
