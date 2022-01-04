package io.flutter.plugins.googlemaps;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

class ClustersController {

    private static final String TAG = "ClustersController";
    private final Map<String, MyClusterItem> clusterItemIdToController;
    private final Map<String, String> googleMapsClusterItemIdToDartMarkerId;
    private ClusterManager<MyClusterItem> clusterManager;
    private final MethodChannel methodChannel;
    private GoogleMap googleMap;

    ClustersController(MethodChannel methodChannel) {
        this.clusterItemIdToController = new HashMap<>();
        this.googleMapsClusterItemIdToDartMarkerId = new HashMap<>();
        this.methodChannel = methodChannel;
    }

    void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    void setClusterManager(ClusterManager<MyClusterItem> mClusterManager) {
        this.clusterManager = mClusterManager;
    }

    ClusterManager<MyClusterItem> getClusterManager() {
        return this.clusterManager;
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
        String markerId = Convert.interpretMarkerOptions(item, markerBuilder);
        MarkerOptions options = markerBuilder.build();
        addClusterItem(markerId, options, markerBuilder.consumeTapEvents());
    }

    private void addClusterItem(String markerId, MarkerOptions markerOptions, boolean consumeTapEvents) {
        LatLng latLng = markerOptions.getPosition();
        MyClusterItem clusterItem =
                new MyClusterItem(
                        latLng.latitude,
                        latLng.longitude,
                        markerOptions.getTitle(),
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
}
