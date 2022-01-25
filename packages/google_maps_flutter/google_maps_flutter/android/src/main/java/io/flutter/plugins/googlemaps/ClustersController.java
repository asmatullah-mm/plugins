package io.flutter.plugins.googlemaps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

@SuppressWarnings("unused")
class ClustersController {

    private final Map<String, MyClusterItem> clusterItemIdToController;
    private final Map<String, String> googleMapsClusterItemIdToDartMarkerId;
    private ClusterManager<MyClusterItem> clusterManager;
    private final MethodChannel methodChannel;
    private ClusterIcon clusterIcon;
    private final Context context;
    private GoogleMap googleMap;

    ClustersController(MethodChannel methodChannel, Context context) {
        this.clusterItemIdToController = new HashMap<>();
        this.googleMapsClusterItemIdToDartMarkerId = new HashMap<>();
        this.methodChannel = methodChannel;
        this.context = context;
    }

    void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    void setClusterIcons(List<Object> iconsToAdd) {
        if (iconsToAdd != null && !iconsToAdd.isEmpty()) {
            List<Bitmap> icons = new ArrayList<>();
            List<Integer> bucket = new ArrayList<>();
            for (Object iconToAdd : iconsToAdd) {
                Convert.interpretClusterIcons(iconToAdd, icons, bucket, context);
            }
            clusterIcon = new ClusterIcon(icons, bucket);
        }
    }

    void setClusterManager(ClusterManager<MyClusterItem> mClusterManager) {
        this.clusterManager = mClusterManager;
        this.clusterManager.setRenderer(new ClustersController.CustomCluster());
    }

    void setClusterListeners(@Nullable ClusterListener listener) {
        this.googleMap.setOnCameraIdleListener(clusterManager);
        this.clusterManager.setOnClusterClickListener(listener);
        this.clusterManager.setOnClusterItemClickListener(listener);
        this.clusterManager.setOnClusterInfoWindowClickListener(listener);
        this.clusterManager.setOnClusterItemInfoWindowClickListener(listener);
        this.clusterManager.setOnClusterItemInfoWindowLongClickListener(listener);
    }

    void addClusterItems(List<Object> itemsToAdd) {
        if (itemsToAdd != null) {
            for (Object itemToAdd : itemsToAdd) {
                addClusterItem(itemToAdd);
            }
            clusterManager.cluster();
        }
    }

    void changeClusterItems(List<Object> ClusterItemToChange) {
        if (ClusterItemToChange != null) {
            for (Object markerToChange : ClusterItemToChange) {
                changeClusterItem(markerToChange);
            }
        }
    }

    void removeClusterItems(List<Object> clusterItemIdsToRemove) {
        if (clusterItemIdsToRemove == null) {
            return;
        }
        for (Object rawClusterItemId : clusterItemIdsToRemove) {
            if (rawClusterItemId == null) {
                continue;
            }
            String clusterItemId = (String) rawClusterItemId;
            final MyClusterItem clusterItemController =
                    clusterItemIdToController.remove(clusterItemId);
            if (clusterItemController != null) {
                googleMapsClusterItemIdToDartMarkerId.remove(
                        clusterItemController.getGoogleMapsClusterItemId());
                clusterManager.removeItem(clusterItemController);
                clusterManager.cluster();
            }
        }
    }

    private void addClusterItem(Object item) {
        if (item == null) {
            return;
        }
        MarkerBuilder markerBuilder = new MarkerBuilder();
        String markerId = Convert.interpretMarkerOptions(item, markerBuilder, context);
        addClusterItem(markerId, markerBuilder, markerBuilder.consumeTapEvents());
    }

    private void addClusterItem(String markerId, MarkerBuilder markerBuilder, boolean consumeTapEvents) {
        MarkerOptions markerOptions = markerBuilder.build();
        LatLng latLng = markerOptions.getPosition();
        MyClusterItem clusterItem =
                new MyClusterItem(
                        latLng.latitude,
                        latLng.longitude,
                        markerOptions.getTitle(),
                        markerBuilder.label(),
                        markerBuilder.customIcon(),
                        markerOptions.getSnippet(),
                        markerId,
                        consumeTapEvents,
                        markerOptions.getIcon());
        this.clusterManager.addItem(clusterItem);
        clusterItemIdToController.put(markerId, clusterItem);
    }

    private void changeClusterItem(Object clusterItem) {
        if (clusterItem == null) {
            return;
        }
        String markerId = getClusterItemId(clusterItem);
        MyClusterItem clusterItemController = clusterItemIdToController.get(markerId);
        // TODO: to be done
//         if (clusterItemController != null) {
//         Convert.interpretMarkerOptions(clusterItem, clusterItemController);
//         }
    }

    @SuppressWarnings("unchecked")
    private static String getClusterItemId(Object clusterItem) {
        Map<String, Object> clusterItemMap = (Map<String, Object>) clusterItem;
        return (String) clusterItemMap.get("markerId");
    }

    public boolean onClusterClick(Cluster<MyClusterItem> cluster) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 600, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        methodChannel.invokeMethod("cluster#onTap", null);
        return true;
    }

    public void onClusterInfoWindowClick(Cluster<MyClusterItem> cluster) {

    }

    public boolean onClusterItemClick(MyClusterItem item) {
        String clusterItemId =
                ((MyClusterItem) item)
                        .getGoogleMapsClusterItemId(); // googleMapsClusterItemIdToDartMarkerId.get(googleMarkerId);
        if (clusterItemId == null) {
            return false;
        }
        methodChannel.invokeMethod("clusterItem#onTap", Convert.markerIdToJson(clusterItemId));
        MyClusterItem clusterController = clusterItemIdToController.get(clusterItemId);
        if (clusterController != null) {
            return clusterController
                    .consumeTapEvents(); // TODO: check it, for now this events is constant.
        }
        return false;
    }

    public void onClusterItemInfoWindowClick(MyClusterItem item) {
        String clusterItemId =
                ((MyClusterItem) item)
                        .getGoogleMapsClusterItemId(); // googleMapsClusterItemIdToDartMarkerId.get(googleMarkerId);
        if (clusterItemId == null) {
            return;
        }
        methodChannel.invokeMethod("clusterItemInfoWindow#onTap", Convert.markerIdToJson(clusterItemId));

    }

    public void onClusterInfoWindowLongClick(Cluster<MyClusterItem> cluster) {

    }

    public void onClusterItemInfoWindowLongClick(MyClusterItem item) {

    }

    /**
     * Custom Cluster class that extends DefaultClusterRenderer
     */
    private class CustomCluster extends DefaultClusterRenderer<MyClusterItem> {
        private final IconGenerator mClusterIconGenerator = new IconGenerator(context);
        private final IconGenerator mMarkerIconGenerator = new IconGenerator(context);
        private final ImageView mClusterImageView;
        private final ImageView mMarkerImageView;

        @SuppressLint("InflateParams")
        public CustomCluster() {
            super(context, googleMap, clusterManager);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View mView = inflater.inflate(R.layout.multi_cluster, null);
            mClusterIconGenerator.setContentView(mView);
            mClusterIconGenerator.setBackground(new ColorDrawable(Color.TRANSPARENT));

            View mMarkerView = inflater.inflate(R.layout.custom_marker, null);
            mMarkerIconGenerator.setContentView(mMarkerView);
            mMarkerIconGenerator.setBackground(new ColorDrawable(Color.TRANSPARENT));

            mClusterImageView = mView.findViewById(R.id.cluster_image);
            mMarkerImageView = mMarkerView.findViewById(R.id.custom_marker_image);
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull MyClusterItem clusterItem, @NonNull MarkerOptions markerOptions) {
            // Draw a single cluster
            if (clusterItem.getCustomIcon() != null) {
//                markerOptions.visible(false);
//                new LoadClusterMarkersAsync(clusterItem, false).execute();
                markerOptions.icon(getItemIcon(clusterItem.getLabel(), clusterItem.getCustomIcon()));
            }
        }

        @Override
        protected void onClusterItemUpdated(@NonNull MyClusterItem clusterItem, @NonNull Marker marker) {
            // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
            if (clusterItem.getCustomIcon() != null) {
//                marker.setVisible(false);
//                new LoadClusterMarkersAsync(clusterItem, false).execute();
                marker.setIcon(getItemIcon(clusterItem.getLabel(), clusterItem.getCustomIcon()));
            }
        }

        private BitmapDescriptor getItemIcon(String label, @NonNull Bitmap bitmap) {
            if (label != null && !label.equals("")) {
                mMarkerImageView.setImageBitmap(bitmap);
                bitmap = mMarkerIconGenerator.makeIcon(label);
            }
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        @Override
        protected void onBeforeClusterRendered(@NonNull Cluster<MyClusterItem> cluster, @NonNull MarkerOptions markerOptions) {
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            if (clusterIcon != null) {
//                markerOptions.visible(false);
//                new LoadClusterMarkersAsync(cluster, true).execute();
                markerOptions.icon(getClusterIcon(cluster));
            }
        }

        @Override
        protected void onClusterUpdated(@NonNull Cluster<MyClusterItem> cluster, @NonNull Marker marker) {
            // Same implementation as onBeforeClusterRendered() (to update cached markers)
            if (clusterIcon != null) {
//                marker.setVisible(false);
//                new LoadClusterMarkersAsync(cluster, true).execute();
                marker.setIcon(getClusterIcon(cluster));
            }
        }

        /**
         * Get a descriptor for custom icon (a cluster) to be used for a marker icon. Note: this
         * method runs on the UI thread. Don't spend too much time in here (like in this example).
         *
         * @param cluster cluster to draw a BitmapDescriptor for
         * @return a BitmapDescriptor representing a cluster
         */
        private BitmapDescriptor getClusterIcon(Cluster<MyClusterItem> cluster) {
            int clusterSize = cluster.getSize();
            List<Integer> bucketList = clusterIcon.getBucket();
            for (int i = 0; i < bucketList.size(); i++) {
                if (clusterSize < bucketList.get(i)) {
                    mClusterImageView.setImageBitmap(clusterIcon.getIcon().get(i));
                    break;
                } else if (i == bucketList.size() - 1) {
                    mClusterImageView.setImageBitmap(clusterIcon.getIcon().get(i));
                }
            }
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }

        @SuppressLint("StaticFieldLeak")
        private class LoadClusterMarkersAsync extends AsyncTask<Void, Void, BitmapDescriptor> {
            private Cluster<MyClusterItem> cluster;
            private MyClusterItem clusterItem;
            private final boolean isCluster;

            public LoadClusterMarkersAsync(Cluster<MyClusterItem> cluster, boolean isCluster) {
                super();
                this.cluster = cluster;
                this.isCluster = isCluster;
            }

            public LoadClusterMarkersAsync(MyClusterItem clusterItem, boolean isCluster) {
                super();
                this.clusterItem = clusterItem;
                this.isCluster = isCluster;
            }

            @Override
            protected BitmapDescriptor doInBackground(Void... v) {
                if (isCluster) {
                    return getClusterIcon(cluster);
                }
                return getItemIcon(clusterItem.getLabel(), clusterItem.getCustomIcon());
            }

            @Override
            protected void onPostExecute(BitmapDescriptor v) {
                super.onPostExecute(v);
                Marker marker;
                if (isCluster) {
                    marker = getMarker(cluster);
                } else {
                    marker = getMarker(clusterItem);
                }
                if (marker != null) {
                    marker.setIcon(v);
//                    marker.setVisible(true);
                }
            }
        }
    }
}
