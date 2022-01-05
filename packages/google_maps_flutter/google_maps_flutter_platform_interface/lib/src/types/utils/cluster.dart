import '../types.dart';
import 'maps_object.dart';

/// Converts an [Iterable] of Markers in a Map of MarkerId -> ClusterItem.
Map<ClusterId, ClusterItem> keyByClusterItemId(Iterable<ClusterItem> markers) {
  return keyByMapsObjectId<ClusterItem>(markers).cast<ClusterId, ClusterItem>();
}

/// Converts a Set of Markers into something serializable in JSON.
Object serializeClusterItemSet(Set<ClusterItem> markers) {
  return serializeMapsObjectSet(markers);
}

/// Converts a List of [ClusterIcon] into something serializable in JSON.
Object serializeClusterIconsList(List<ClusterIcon> clusterIcons) {
  return clusterIcons.map((ClusterIcon e) => e.toJson()).toList();
}
