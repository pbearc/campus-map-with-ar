package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements OnItemClickListener {

    private RecyclerView recyclerView;
    private SectionAdapter sectionAdapter;
    private List<SectionItem> itemList;
    private ImageView profileImageView;
    private EditText editTextName, editTextEmail, editTextPhone;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Views
        profileImageView = findViewById(R.id.profileImageView);



        // Set a click listener on the profile picture to change it
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle profile image change (not implemented here)
                Toast.makeText(ProfileActivity.this, "Change profile picture", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Prepare data
        itemList = new ArrayList<>();
        itemList.add(new SectionItem(SectionItem.TYPE_HEADER, "Today, 4 Oct", null));
        itemList.add(new SectionItem(SectionItem.TYPE_ITEM, "FIT3181 Tutorial", "2408 | Discussion Room"));
        itemList.add(new SectionItem(SectionItem.TYPE_ITEM, "FIT2102 Tutorial","2435"));
        itemList.add(new SectionItem(SectionItem.TYPE_HEADER, "Monday, 7 Oct",null));
        itemList.add(new SectionItem(SectionItem.TYPE_ITEM, "FIT3162 Lecture","2407"));
        itemList.add(new SectionItem(SectionItem.TYPE_ITEM, "FIT 3143 Tutorial","2420"));
        itemList.add(new SectionItem(SectionItem.TYPE_HEADER, "Tuesday, 8 Oct",null));
        itemList.add(new SectionItem(SectionItem.TYPE_ITEM, "FIT3162 Lecture","2416"));
        itemList.add(new SectionItem(SectionItem.TYPE_ITEM, "FIT 3143 Tutorial","2419"));

        // Set adapter
        sectionAdapter = new SectionAdapter(itemList, this);
        recyclerView.setAdapter(sectionAdapter);
    }




    @Override
    public void onItemClick(SectionItem item) {
        // Handle item click event
        Toast.makeText(this, "Clicked: " + item.getText(), Toast.LENGTH_SHORT).show();
        // Wrap the item's text in an Intent
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.putExtra("item_text", item.getLocation()); // Add the item's text as an extra
        startActivity(intent); // Start MainActivity
    }


}

