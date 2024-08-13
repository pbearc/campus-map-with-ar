package com.example.myapplication;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TwoDViewFragment extends Fragment {

    private MapView mapView;
    private RecyclerView floorRecyclerView;
    private FloorAdapter floorAdapter;
    private ArrayList<Integer> allFloors;
    private Retrofit retrofit;
    private NavApi navApiInterface;
    private String BASE_URL = "http://10.0.2.2:5000";
    List<PointOfInterest> allDestinations;
    List<PointOfInterest> filteredDestinations;
    DestinationAdapter destinationAdapter;
    final int DEFAULT_FLOOR = 4;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TwoDViewFragment(List<PointOfInterest> allDestinations, List<PointOfInterest> filteredDestinations, DestinationAdapter destinationAdapter) {
        this.allDestinations = allDestinations;
        this.filteredDestinations = filteredDestinations;
        this.destinationAdapter = destinationAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        navApiInterface = retrofit.create(NavApi.class);
        return inflater.inflate(R.layout.fragment_two_d_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        floorRecyclerView = view.findViewById(R.id.floor_recycler_view);

        floorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true));
        floorRecyclerView.setHasFixedSize(true);
        allFloors = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            allFloors.add(i);
        }
        floorAdapter = new FloorAdapter(this, getContext(), allFloors, allDestinations, filteredDestinations, destinationAdapter);
        floorRecyclerView.setAdapter(floorAdapter);

        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        initMap();
    }

    public void initMap(){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        navApiInterface.getFloorMap(DEFAULT_FLOOR).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                try {
                                    Bitmap icon = BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_destination));
                                    style.addImage(getString(R.string.dest_icon_img), icon);
                                    addStyle(style, response.body());
                                } catch (Throwable throwable) {
                                    Log.e("TwoDViewFragment", throwable.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable throwable) {
                                Log.e("TwoDViewFragment", throwable.getMessage());
                            }
                        });
                    }
                });
            }
        });
    }

    public void updateMap(String geojsonMapData){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        try {
                            style.removeLayer(getString(R.string.poi_layout_id));
                            style.removeLayer(getString(R.string.poi_fill_id));
                            style.removeLayer(getString(R.string.poi_title_id));
                            style.removeSource(getString(R.string.floor_map_id));
                            addStyle(style, geojsonMapData);
                        } catch (Throwable throwable) {
                            Log.e("TwoDViewFragment", throwable.getMessage());
                        }
                    }
                });
            }
        });
    }

    public void addStyle(Style style, String source){
        GeoJsonSource floorMap = new GeoJsonSource(getString(R.string.floor_map_id), source);
        style.addSource(floorMap);

        LineLayer poiLayout = new LineLayer(getString(R.string.poi_layout_id), getString(R.string.floor_map_id));
        style.addLayer(poiLayout);

        FillLayer poiFill = new FillLayer(getString(R.string.poi_fill_id), getString(R.string.floor_map_id))
                .withProperties(
                        fillColor(Color.parseColor("#6db682")),
                        fillOpacity(0.5f)
                )
                .withFilter(
                        Expression.eq(Expression.get("isRoute"), Expression.literal(false))
                );
        style.addLayer(poiFill);

        SymbolLayer poiTitle = new SymbolLayer(getString(R.string.poi_title_id), getString(R.string.floor_map_id))
                .withProperties(
                        textField(Expression.get("title")),
                        textSize(10f)
                )
                .withFilter(
                        Expression.eq(Expression.get("isRoute"), Expression.literal(false))
                );
        style.addLayer(poiTitle);
    }

    public void addDestination(PointOfInterest destination){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        SymbolLayer destinationIcon = new SymbolLayer(getString(R.string.dest_icon_id), getString(R.string.floor_map_id))
                                .withProperties(
                                        iconImage(getString(R.string.dest_icon_img)),
                                        iconSize(0.5f),
                                        iconAllowOverlap(true)
                                )
                                .withFilter(
                                        Expression.eq(Expression.get("identifier"), Expression.literal(destination.getIdentifier()))
                                );
                        LineLayer destinationLayout = new LineLayer(getString(R.string.destination_layout_id), getString(R.string.floor_map_id))
                                .withProperties(
                                    lineWidth(3f),
                                    lineColor("#FF0000")
                                )
                                .withFilter(
                                        Expression.eq(Expression.get("identifier"), Expression.literal(destination.getIdentifier()))
                                );
                        style.addLayer(destinationIcon);
                        style.addLayer(destinationLayout);
                    }
                });
            }
        });
    }

    public void removeDestination(){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        style.removeLayer(getString(R.string.dest_icon_id));
                        style.removeLayer(getString(R.string.destination_layout_id));
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}