package com.example.myapplication;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.ViewHolder> {

    private ArrayList<Integer> floors;
    private Context context;
    private Retrofit retrofit;
    private Retrofit mapRetrofit;
    private NavApi navApiInterface;
    private NavApi mapNavApiInterface;
    private String BASE_URL = "http://10.0.2.2:5000";
    List<PointOfInterest> allDestinations;
    List<PointOfInterest> filteredDestinations;
    DestinationAdapter destinationAdapter;
    TwoDViewFragment instance;

    public FloorAdapter(TwoDViewFragment instance, Context context, ArrayList<Integer> floors, List<PointOfInterest> allDestinations, List<PointOfInterest> filteredDestinations, DestinationAdapter destinationAdapter) {
        this.instance = instance;
        this.context = context;
        this.floors = floors;
        this.allDestinations = allDestinations;
        this.filteredDestinations = filteredDestinations;
        this.destinationAdapter = destinationAdapter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mapRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        navApiInterface = retrofit.create(NavApi.class);
        mapNavApiInterface = mapRetrofit.create(NavApi.class);

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_floor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.floor_no.setText(Integer.toString(floors.get(position)));
        final int fPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int floor_no = floors.get(fPosition);
                mapNavApiInterface.getFloorMap(floor_no).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        instance.updateMap(response.body());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        Log.d("FloorAdapter", throwable.getMessage());
                    }
                });

                navApiInterface.getPOIs(floor_no).enqueue(new Callback<List<PointOfInterest>>() {
                    @Override
                    public void onResponse(Call<List<PointOfInterest>> call, Response<List<PointOfInterest>> response) {
                        allDestinations.clear();
                        allDestinations.addAll(response.body());
                        filteredDestinations.clear();
                        filteredDestinations.addAll(response.body());
                        destinationAdapter.filterList(filteredDestinations);
                    }

                    @Override
                    public void onFailure(Call<List<PointOfInterest>> call, Throwable throwable) {
                        Log.e("FloorAdapter", throwable.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return floors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView floor_no;

        ViewHolder(View itemView) {
            super(itemView);
            floor_no = itemView.findViewById(R.id.floor_no);
        }
    }
}
