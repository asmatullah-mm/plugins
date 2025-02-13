// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.collections.MarkerManager;
import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MarkersController {

  private static final String TAG = "MarkersController";
  private final Map<String, MarkerController> markerIdToController;
  private final Map<String, String> googleMapsMarkerIdToDartMarkerId;
  private final MethodChannel methodChannel;
  private MarkerManager markerManager;
  private GoogleMap googleMap;
  private final Context context;
  private GoogleMap.OnMarkerClickListener onMarkerClickListener;
  private GoogleMap.OnMarkerDragListener onMarkerDragListener;
  private GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener;

  MarkersController(MethodChannel methodChannel, Context context) {
    this.markerIdToController = new HashMap<>();
    this.googleMapsMarkerIdToDartMarkerId = new HashMap<>();
    this.methodChannel = methodChannel;
    this.context = context;
  }

  void setGoogleMap(GoogleMap googleMap) {
    this.googleMap = googleMap;
  }

  void setMarkerManager(MarkerManager markerManager) {
    this.markerManager = markerManager;
  }

  MarkerManager getMarkerManager() {
    return this.markerManager;
  }
  
  void addMarkersListener(@Nullable GoogleMapListener listener) {
    this.onMarkerClickListener = listener;
    this.onMarkerDragListener = listener;
    this.onInfoWindowClickListener = listener;
  }

  void addMarkers(List<Object> markersToAdd) {
    if (markersToAdd != null) {
      for (Object markerToAdd : markersToAdd) {
        addMarker(markerToAdd);
      }
    }
  }

  void changeMarkers(List<Object> markersToChange) {
    if (markersToChange != null) {
      for (Object markerToChange : markersToChange) {
        changeMarker(markerToChange);
      }
    }
  }

  void removeMarkers(List<Object> markerIdsToRemove) {
    if (markerIdsToRemove == null) {
      return;
    }
    for (Object rawMarkerId : markerIdsToRemove) {
      if (rawMarkerId == null) {
        continue;
      }
      String markerId = (String) rawMarkerId;
      final MarkerController markerController = markerIdToController.remove(markerId);
      if (markerController != null) {
        markerController.remove();
        googleMapsMarkerIdToDartMarkerId.remove(markerController.getGoogleMapsMarkerId());
      }
    }
  }

  void showMarkerInfoWindow(String markerId, MethodChannel.Result result) {
    MarkerController markerController = markerIdToController.get(markerId);
    if (markerController != null) {
      markerController.showInfoWindow();
      result.success(null);
    } else {
      result.error("Invalid markerId", "showInfoWindow called with invalid markerId", null);
    }
  }

  void hideMarkerInfoWindow(String markerId, MethodChannel.Result result) {
    MarkerController markerController = markerIdToController.get(markerId);
    if (markerController != null) {
      markerController.hideInfoWindow();
      result.success(null);
    } else {
      result.error("Invalid markerId", "hideInfoWindow called with invalid markerId", null);
    }
  }

  void isInfoWindowShown(String markerId, MethodChannel.Result result) {
    MarkerController markerController = markerIdToController.get(markerId);
    if (markerController != null) {
      result.success(markerController.isInfoWindowShown());
    } else {
      result.error("Invalid markerId", "isInfoWindowShown called with invalid markerId", null);
    }
  }

  boolean onMarkerTap(String googleMarkerId) {
    String markerId = googleMapsMarkerIdToDartMarkerId.get(googleMarkerId);
    if (markerId == null) {
      return false;
    }
    methodChannel.invokeMethod("marker#onTap", Convert.markerIdToJson(markerId));
    MarkerController markerController = markerIdToController.get(markerId);
    if (markerController != null) {
      return markerController.consumeTapEvents();
    }
    return false;
  }

  void onMarkerDragStart(String googleMarkerId, LatLng latLng) {
    String markerId = googleMapsMarkerIdToDartMarkerId.get(googleMarkerId);
    if (markerId == null) {
      return;
    }
    final Map<String, Object> data = new HashMap<>();
    data.put("markerId", markerId);
    data.put("position", Convert.latLngToJson(latLng));
    methodChannel.invokeMethod("marker#onDragStart", data);
  }

  void onMarkerDrag(String googleMarkerId, LatLng latLng) {
    String markerId = googleMapsMarkerIdToDartMarkerId.get(googleMarkerId);
    if (markerId == null) {
      return;
    }
    final Map<String, Object> data = new HashMap<>();
    data.put("markerId", markerId);
    data.put("position", Convert.latLngToJson(latLng));
    methodChannel.invokeMethod("marker#onDrag", data);
  }

  void onMarkerDragEnd(String googleMarkerId, LatLng latLng) {
    String markerId = googleMapsMarkerIdToDartMarkerId.get(googleMarkerId);
    if (markerId == null) {
      return;
    }
    final Map<String, Object> data = new HashMap<>();
    data.put("markerId", markerId);
    data.put("position", Convert.latLngToJson(latLng));
    methodChannel.invokeMethod("marker#onDragEnd", data);
  }

  void onInfoWindowTap(String googleMarkerId) {
    String markerId = googleMapsMarkerIdToDartMarkerId.get(googleMarkerId);
    if (markerId == null) {
      return;
    }
    methodChannel.invokeMethod("infoWindow#onTap", Convert.markerIdToJson(markerId));
  }

  private void addMarker(Object marker) {
    if (marker == null) {
      return;
    }
    MarkerBuilder markerBuilder = new MarkerBuilder();
    String markerId = Convert.interpretMarkerOptions(marker, markerBuilder, context);
    MarkerOptions options = markerBuilder.build();
    addMarker(markerId, options, markerBuilder.consumeTapEvents());
  }

  private void addMarker(String markerId, MarkerOptions markerOptions, boolean consumeTapEvents) {
    MarkerManager.Collection collection = markerManager.newCollection();
    final Marker marker = collection.addMarker(markerOptions);
    setMarkersListener(collection);
    MarkerController controller = new MarkerController(marker, consumeTapEvents);
    markerIdToController.put(markerId, controller);
    googleMapsMarkerIdToDartMarkerId.put(marker.getId(), markerId);
  }

  private void setMarkersListener(MarkerManager.Collection collection){
    collection.setOnMarkerClickListener(this.onMarkerClickListener);
    collection.setOnMarkerDragListener(this.onMarkerDragListener);
    collection.setOnInfoWindowClickListener(this.onInfoWindowClickListener);
  }

  private void changeMarker(Object marker) {
    if (marker == null) {
      return;
    }
    String markerId = getMarkerId(marker);
    MarkerController markerController = markerIdToController.get(markerId);
    if (markerController != null) {
      Convert.interpretMarkerOptions(marker, markerController, context);
    }
  }

  @SuppressWarnings("unchecked")
  private static String getMarkerId(Object marker) {
    Map<String, Object> markerMap = (Map<String, Object>) marker;
    return (String) markerMap.get("markerId");
  }
}
