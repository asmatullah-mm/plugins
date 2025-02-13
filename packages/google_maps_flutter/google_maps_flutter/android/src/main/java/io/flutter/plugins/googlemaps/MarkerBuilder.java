// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import android.graphics.Bitmap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

class MarkerBuilder implements MarkerOptionsSink {
  private final MarkerOptions markerOptions;
  private boolean consumeTapEvents;
  private String label;
  private Bitmap customIcon;

  MarkerBuilder() {
    this.markerOptions = new MarkerOptions();
  }

  MarkerOptions build() {
    return markerOptions;
  }

  boolean consumeTapEvents() {
    return consumeTapEvents;
  }
  
  String label() {
    return label;
  }

  Bitmap customIcon() {
    return customIcon;
  }

  @Override
  public void setAlpha(float alpha) {
    markerOptions.alpha(alpha);
  }

  @Override
  public void setAnchor(float u, float v) {
    markerOptions.anchor(u, v);
  }

  @Override
  public void setConsumeTapEvents(boolean consumeTapEvents) {
    this.consumeTapEvents = consumeTapEvents;
  }

  @Override
  public void setDraggable(boolean draggable) {
    markerOptions.draggable(draggable);
  }

  @Override
  public void setFlat(boolean flat) {
    markerOptions.flat(flat);
  }

  @Override
  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public void setCustomIcon(Bitmap bitmap) {
    this.customIcon = bitmap;
  }

  @Override
  public void setIcon(BitmapDescriptor bitmapDescriptor) {
    markerOptions.icon(bitmapDescriptor);
  }

  @Override
  public void setInfoWindowAnchor(float u, float v) {
    markerOptions.infoWindowAnchor(u, v);
  }

  @Override
  public void setInfoWindowText(String title, String snippet) {
    markerOptions.title(title);
    markerOptions.snippet(snippet);
  }

  @Override
  public void setPosition(LatLng position) {
    markerOptions.position(position);
  }

  @Override
  public void setRotation(float rotation) {
    markerOptions.rotation(rotation);
  }

  @Override
  public void setVisible(boolean visible) {
    markerOptions.visible(visible);
  }

  @Override
  public void setZIndex(float zIndex) {
    markerOptions.zIndex(zIndex);
  }
}
