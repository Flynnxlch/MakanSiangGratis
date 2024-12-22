package com.example.makansianggratis.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.makansianggratis.Fragment.ContactFragment;
import com.example.makansianggratis.Fragment.HistoryFragment;
import com.example.makansianggratis.Fragment.HomeFragment;
import com.example.makansianggratis.Fragment.ProfileFragment;
import com.example.makansianggratis.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView BnV;
    HomeFragment HomeF;
    HistoryFragment HistoryF;
    ContactFragment ContactF;
    ProfileFragment AboutF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BnV = findViewById(R.id.bottomenu);

        HomeF = new HomeFragment();
        HistoryF = new HistoryFragment();
        ContactF = new ContactFragment();
        AboutF = new ProfileFragment();

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, HomeF)
                    .commit();
        }

        BnV.setOnItemSelectedListener(item -> {
            Fragment selectedFrag = null;

            if (item.getItemId() == R.id.navigation_home) {
                selectedFrag = HomeF;
            } else if (item.getItemId() == R.id.navigation_history) {
                selectedFrag = HistoryF;
            } else if (item.getItemId() == R.id.navigation_contact) {
                selectedFrag = ContactF;
            } else if (item.getItemId() == R.id.navigation_profile) {
                selectedFrag = AboutF;
            }

            if (selectedFrag != null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFrag)
                        .commit();
            }
            return true;
        });
    }
}