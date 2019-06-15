package WalkTogether.com.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import WalkTogether.com.R;

public class MatchFragment extends Fragment {

    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    private Button match;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_match, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        match = getActivity().findViewById(R.id.match);

        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();

        match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Match().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            Log.d("check_Match", String.valueOf(task.getResult()));
                            if( String.valueOf(task.getResult()).equals("ok")){
                                Toast.makeText(getContext(), "配對完成，可以一起走路囉！", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), "尚未有可以配對的人，將在配對完成後通知...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

    }

    private Task<String> Match() {
        // Create the arguments to the callable function.
        Log.d("check_Match", String.valueOf(mAuth.getUid()));
        Map<String, Object> data = new HashMap<>();
        data.put("user", mAuth.getUid());

        return mFunctions
                .getHttpsCallable("Match")
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