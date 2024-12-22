package com.example.makansianggratis.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.makansianggratis.Adapter.ContactAdapter;
import com.example.makansianggratis.Model.CC;
import com.example.makansianggratis.R;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<CC> contactList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize contact list
        contactList = new ArrayList<>();
        populateContacts(); // Populate the list with contacts

        // Set up adapter
        contactAdapter = new ContactAdapter(requireContext(), contactList);
        recyclerViewContacts.setAdapter(contactAdapter);

    }

    private void populateContacts() {
        // Add contacts manually
        contactList.add(new CC("Pak Beni FST", "Beni@example.com", "+6287725277933"));
        contactList.add(new CC("Pak Jalu FST", "Jalu@example.com", "+6283813100405"));
        contactList.add(new CC("Michael Johnson", "michael.j@example.com", "+1122334455"));
        contactList.add(new CC("Emily Davis", "emily.davis@example.com", "+5566778899"));
        // Add more contacts as needed
    }
}
