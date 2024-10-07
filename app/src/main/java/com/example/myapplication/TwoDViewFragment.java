package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.internal.view.SupportMenu;
import androidx.fragment.app.Fragment;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class TwoDViewFragment extends Fragment {
    private int currentFloor;
    private boolean initializedMap = false;
    /* access modifiers changed from: private */
    public MapView mapView;
    /* access modifiers changed from: private */
    public NavApi navApiInterface;
    /* access modifiers changed from: private */
    public LineManager navRouteManager;
    private Retrofit retrofit;
    /* access modifiers changed from: private */
    public SymbolManager userPositionManager;

    public boolean isInitializedMap() {
        return this.initializedMap;
    }

    public TwoDViewFragment() {
        Retrofit.Builder builder = new Retrofit.Builder();
        MainActivity mainActivity = (MainActivity) getActivity();
        this.retrofit = builder.baseUrl(MainActivity.BASE_URL).addConverterFactory(ScalarsConverterFactory.create()).build();
        this.navApiInterface = (NavApi) this.retrofit.create(NavApi.class);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        return inflater.inflate(R.layout.fragment_two_d_view, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mapView = (MapView) view.findViewById(R.id.map_view);
        this.mapView.onCreate(savedInstanceState);
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(final MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.LIGHT, (Style.OnStyleLoaded) new Style.OnStyleLoaded() {
                    public void onStyleLoaded(Style style) {
                        Bitmap destination_icon = BitmapUtils.getBitmapFromDrawable(TwoDViewFragment.this.getResources().getDrawable(R.drawable.ic_destination));
                        Bitmap user_position_icon = BitmapUtils.getBitmapFromDrawable(TwoDViewFragment.this.getResources().getDrawable(R.drawable.ic_user_position));
                        style.addImage(TwoDViewFragment.this.getString(R.string.dest_icon_img), destination_icon);
                        style.addImage(TwoDViewFragment.this.getString(R.string.user_position_icon_img), user_position_icon);
                        SymbolManager unused = TwoDViewFragment.this.userPositionManager = new SymbolManager(TwoDViewFragment.this.mapView, mapboxMap, style);
                        userPositionManager.setIconAllowOverlap(true);
                        userPositionManager.setIconIgnorePlacement(true);
                        TwoDViewFragment.this.userPositionManager.setIconAllowOverlap(true);
                        LineManager unused2 = TwoDViewFragment.this.navRouteManager = new LineManager(TwoDViewFragment.this.mapView, mapboxMap, style);
                    }
                });
            }
        });
        this.initializedMap = true;
    }

    public void updateMap(final int floor) {
        if (floor != this.currentFloor) {
            this.currentFloor = floor;
            this.mapView.getMapAsync(new OnMapReadyCallback() {
                public void onMapReady(MapboxMap mapboxMap) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        public void onStyleLoaded(final Style style) {
                            style.removeLayer(TwoDViewFragment.this.getString(R.string.poi_layout_id));
                            style.removeLayer(TwoDViewFragment.this.getString(R.string.poi_fill_id));
                            style.removeLayer(TwoDViewFragment.this.getString(R.string.poi_title_id));
                            TwoDViewFragment.this.removeDestination();
                            style.removeSource(TwoDViewFragment.this.getString(R.string.floor_map_id));
                            TwoDViewFragment.this.navApiInterface.getFloorMap(floor).enqueue(new Callback<String>() {
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try {
                                        TwoDViewFragment.this.addStyle(style, response.body());
                                        TwoDViewFragment.this.addDestination(((MainActivity) TwoDViewFragment.this.getActivity()).getCurrentDestination());
                                    } catch (Throwable throwable) {
                                        Log.e("TwoDViewFragment", throwable.getMessage());
                                    }
                                }

                                public void onFailure(Call<String> call, Throwable throwable) {
                                    Log.e("TwoDViewFragment", throwable.getMessage());
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    public void addStyle(Style style, String source) {
        style.addSource(new GeoJsonSource(getString(R.string.floor_map_id), source));
        //style.addLayer(new LineLayer(getString(R.string.poi_layout_id), getString(R.string.floor_map_id)));
        SymbolLayer symbolLayer = new SymbolLayer(getString(R.string.poi_title_id), getString(R.string.floor_map_id)).withProperties(PropertyFactory.textField(Expression.get("title")), PropertyFactory.textSize(Float.valueOf(10.0f))).withFilter(Expression.eq(Expression.get("isRoute"), Expression.literal(false)));
        LineLayer lineLayer = new LineLayer(getString(R.string.poi_layout_id), getString(R.string.floor_map_id));
        FillLayer fillLayer = new FillLayer(getString(R.string.poi_fill_id), getString(R.string.floor_map_id)).withProperties(PropertyFactory.fillColor(Color.parseColor("#6db682")), PropertyFactory.fillOpacity(Float.valueOf(0.5f))).withFilter(Expression.eq(Expression.get("isRoute"), Expression.literal(false)));
        if (style.getLayer(getString(R.string.destination_layout_id)) == null){
            style.addLayer(lineLayer);
        } else {
            style.addLayerBelow(lineLayer, getString(R.string.destination_layout_id));
        }
        if (style.getLayer(getString(R.string.dest_icon_id)) == null){
            style.addLayer(fillLayer);
            style.addLayer(symbolLayer);
        } else {
            style.addLayerBelow(fillLayer, getString(R.string.dest_icon_id));
            style.addLayerBelow(symbolLayer, getString(R.string.dest_icon_id));
        }
        //style.addLayer(new FillLayer(getString(R.string.poi_fill_id), getString(R.string.floor_map_id)).withProperties(PropertyFactory.fillColor(Color.parseColor("#6db682")), PropertyFactory.fillOpacity(Float.valueOf(0.5f))).withFilter(Expression.eq(Expression.get("isRoute"), Expression.literal(false))));
        //style.addLayer(new SymbolLayer(getString(R.string.poi_title_id), getString(R.string.floor_map_id)).withProperties(PropertyFactory.textField(Expression.get("title")), PropertyFactory.textSize(Float.valueOf(10.0f))).withFilter(Expression.eq(Expression.get("isRoute"), Expression.literal(false))));
    }

    public void addDestination(final PointOfInterest destination) {
        if (destination != null && destination.getZ() == this.currentFloor) {
            this.mapView.getMapAsync(new OnMapReadyCallback() {
                public void onMapReady(MapboxMap mapboxMap) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        public void onStyleLoaded(Style style) {
                            SymbolLayer destinationIcon = new SymbolLayer(TwoDViewFragment.this.getString(R.string.dest_icon_id), TwoDViewFragment.this.getString(R.string.floor_map_id)).withProperties(PropertyFactory.iconImage(TwoDViewFragment.this.getString(R.string.dest_icon_img)), PropertyFactory.iconSize(Float.valueOf(0.5f)), PropertyFactory.iconAllowOverlap((Boolean) true)).withFilter(Expression.eq(Expression.get("identifier"), Expression.literal(destination.getIdentifier())));
                            LineLayer destinationLayout = new LineLayer(TwoDViewFragment.this.getString(R.string.destination_layout_id), TwoDViewFragment.this.getString(R.string.floor_map_id)).withProperties(PropertyFactory.lineWidth(Float.valueOf(3.0f)), PropertyFactory.lineColor("#FF0000")).withFilter(Expression.eq(Expression.get("identifier"), Expression.literal(destination.getIdentifier())));
                            style.addLayer(destinationIcon);
                            style.addLayer(destinationLayout);

                            //style.addLayerAbove(destinationIcon, getString(R.string.poi_title_id));
                            //style.addLayerAbove(destinationLayout, getString(R.string.poi_layout_id));
                        }
                    });
                }
            });
        }
    }

    public void removeDestination() {
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    public void onStyleLoaded(Style style) {
                        style.removeLayer(TwoDViewFragment.this.getString(R.string.dest_icon_id));
                        style.removeLayer(TwoDViewFragment.this.getString(R.string.destination_layout_id));
                    }
                });
            }
        });
    }

    public void addUserPosition(final Double longitude, final Double latitude) {
        Log.d("TwoDViewFragment", "Latitude: " + Double.toString(latitude.doubleValue()) + " Longitude: " + Double.toString(longitude.doubleValue()));
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    public void onStyleLoaded(Style style) {
                        TwoDViewFragment.this.userPositionManager.deleteAll();
                        TwoDViewFragment.this.userPositionManager.create(new SymbolOptions().withLatLng(new LatLng(longitude.doubleValue(), latitude.doubleValue())).withIconImage(TwoDViewFragment.this.getString(R.string.user_position_icon_img)).withIconSize(Float.valueOf(0.08f)));
                    }
                });
            }
        });
    }

    public void addRoute(final List<List<Double>> listRoute) {
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    public void onStyleLoaded(Style style) {
                        List<LatLng> latLongs = new ArrayList<>();
                        for (List<Double> coordinate : listRoute) {
                            latLongs.add(new LatLng(coordinate.get(1).doubleValue(), coordinate.get(0).doubleValue()));
                        }
                        TwoDViewFragment.this.navRouteManager.create(new LineOptions().withLatLngs(latLongs).withLineColor(ColorUtils.colorToRgbaString(Color.RED)).withLineWidth(Float.valueOf(5.0f)));
                    }
                });
            }
        });
    }

    public void removeRoute() {
        if (this.navRouteManager != null) {
            this.navRouteManager.deleteAll();
        }
    }

    public void updateCameraBearing(final Double bearing) {
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(mapboxMap.getCameraPosition().target).bearing(bearing.doubleValue()).build()), 1000);
            }
        });
    }

    public void updateCameraPosition(final Double latitude, final Double longitude) {
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(latitude.doubleValue(), longitude.doubleValue())).bearing(mapboxMap.getCameraPosition().bearing).build()));
            }
        });
    }

    public void onStart() {
        super.onStart();
        this.mapView.onStart();
    }

    public void onStop() {
        super.onStop();
        this.mapView.onStop();
    }

    public void onResume() {
        super.onResume();
        this.mapView.onResume();
    }

    public void onPause() {
        super.onPause();
        this.mapView.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mapView.onSaveInstanceState(outState);
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mapView.onDestroy();
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.mapView.onLowMemory();
    }
}