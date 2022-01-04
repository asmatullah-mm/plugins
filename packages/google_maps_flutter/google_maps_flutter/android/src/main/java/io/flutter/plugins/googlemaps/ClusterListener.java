// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;


import com.google.maps.android.clustering.ClusterManager;

interface ClusterListener
        extends
        ClusterManager.OnClusterClickListener<MyClusterItem>,
        ClusterManager.OnClusterItemClickListener<MyClusterItem>,
        ClusterManager.OnClusterInfoWindowClickListener<MyClusterItem>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MyClusterItem>,
        ClusterManager.OnClusterInfoWindowLongClickListener<MyClusterItem>,
        ClusterManager.OnClusterItemInfoWindowLongClickListener<MyClusterItem> {
}
