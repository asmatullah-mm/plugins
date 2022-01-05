package io.flutter.plugins.googlemaps;

import android.graphics.Bitmap;

import java.util.List;

public class ClusterIcon {
    private final List<Bitmap> icons;
    private final List<Integer> bucket;

    public ClusterIcon(List<Bitmap> icon, List<Integer> bucket) {
        this.icons = icon;
        this.bucket = bucket;
    }

    public List<Bitmap> getIcon() {
        return icons;
    }

    public List<Integer> getBucket() {
        return bucket;
    }
}
