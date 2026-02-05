package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");
        citiesRef.addSnapshotListener((value,error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for(QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }

        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        // Delete old document and create new one with updated data
        citiesRef.document(city.getName()).delete()
                .addOnSuccessListener(unused -> {
                    city.setName(title);
                    city.setProvince(year);
                    citiesRef.document(title).set(city)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "City updated"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error updating city", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting old city", e));
    }

    @Override
    public void deleteCity(City city) {
        citiesRef.document(city.getName()).delete()
                .addOnSuccessListener(unused -> Log.d("Firestore", "City deleted: " + city.getName()))
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting city", e));
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        citiesRef.document(city.getName()).set(city)
                .addOnSuccessListener(unused -> Log.d("Firestore", "Document added"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding city", e));

    }

}