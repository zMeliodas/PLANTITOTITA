package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meliodas.plantitotita.R;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executor;

public class HomePageFragment extends Fragment {

    private TextView helloTxtView;
    private String displayName;
    FirebaseFirestore fStore;
    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        helloTxtView = view.findViewById(R.id.helloTxtView);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = fStore.collection("users").document(firebaseUser.getUid());
        if (getActivity() == null) return view;
        documentReference.addSnapshotListener(getActivity(), (value, error) -> {
            if (value != null) {

                displayName = value.getString("user_name");
                helloTxtView.setText("Hello, " + displayName + "!");
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}