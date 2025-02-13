import 'package:flutter/material.dart';
import 'types.dart';

Object _offsetToJson(Offset offset) {
  return <Object>[offset.dx, offset.dy];
}

/// Bucket size for cluster icons based on cluster size.
enum BucketSize {
  /// SMALL Icon for cluster
  SMALL,

  /// MEDIUM Icon for cluster
  MEDIUM,

  /// LARGE Icon for cluster
  LARGE
}

/// Cluster Icons for clusters.
class ClusterIcon {
  /// Creates an immutable representation of a label on for [Marker].
  const ClusterIcon({
    required this.icon,
    required this.bucket,
  });

  /// A description of the bitmap used to draw the marker icon.
  final BitmapDescriptor icon;

  /// Bucket size for cluster [title].
  ///
  /// A null value means no additional text.
  final BucketSize bucket;

  /// Creates a new [ClusterIcon] object whose values are the same as this instance,
  /// unless overwritten by the specified parameters.
  ClusterIcon copyWith({
    BitmapDescriptor? iconParam,
    BucketSize? bucketParam,
  }) {
    return ClusterIcon(
      icon: iconParam ?? icon,
      bucket: bucketParam ?? bucket,
    );
  }

  /// Converts [ClusterIcon] into json
  Object toJson() {
    final Map<String, Object> json = <String, Object>{};

    void addIfPresent(String fieldName, Object? value) {
      if (value != null) {
        json[fieldName] = value;
      }
    }

    addIfPresent('icon', icon.toJson());
    addIfPresent('bucket', _toInt(bucket));

    return json;
  }

  int _toInt(BucketSize bucket) {
    switch (bucket) {
      case BucketSize.SMALL:
        return 100;
      case BucketSize.MEDIUM:
        return 500;
      case BucketSize.LARGE:
        return 1000;
    }
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    if (other.runtimeType != runtimeType) return false;
    final ClusterIcon typedOther = other as ClusterIcon;
    return icon == typedOther.icon && bucket == typedOther.bucket;
  }

  @override
  int get hashCode => hashValues(icon.hashCode, bucket);

  @override
  String toString() {
    return 'ClusterIcon{icon: $icon, bucket: $bucket}';
  }
}

/// Uniquely identifies a [ClusterItem] among [GoogleMap] markers.
///
/// This does not have to be globally unique, only unique among the list.
@immutable
class ClusterId extends MapsObjectId<ClusterItem> {
  /// Creates an immutable identifier for a [Marker].
  const ClusterId(String value) : super(value);
}

/// Marks a cluster of geographical locations on the map.
///
/// A cluster icon is drawn oriented against the device's screen rather than
/// the map's surface; that is, it will not necessarily change orientation
/// due to map rotations, tilting, or zooming.
@immutable
class ClusterItem implements MapsObject {
  /// Creates a set of marker configuration options.
  ///
  /// Default marker options.
  ///
  /// Specifies a marker that
  /// * is fully opaque; [alpha] is 1.0
  /// * uses icon bottom center to indicate map position; [anchor] is (0.5, 1.0)
  /// * has default tap handling; [consumeTapEvents] is false
  /// * is stationary; [draggable] is false
  /// * is drawn against the screen, not the map; [flat] is false
  /// * has a default icon; [icon] is `BitmapDescriptor.defaultMarker`
  /// * anchors the info window at top center; [infoWindowAnchor] is (0.5, 0.0)
  /// * has no info window text; [infoWindowText] is `InfoWindowText.noText`
  /// * is positioned at 0, 0; [position] is `LatLng(0.0, 0.0)`
  /// * has an axis-aligned icon; [rotation] is 0.0
  /// * is visible; [visible] is true
  /// * is placed at the base of the drawing order; [zIndex] is 0.0
  /// * reports [onTap] events
  /// * reports [onDragEnd] events
  const ClusterItem({
    required this.markerId,
    this.alpha = 1.0,
    this.label,
    this.anchor = const Offset(0.5, 1.0),
    this.consumeTapEvents = false,
    this.draggable = false,
    this.flat = false,
    this.icon = BitmapDescriptor.defaultMarker,
    this.infoWindow = InfoWindow.noText,
    this.position = const LatLng(0.0, 0.0),
    this.rotation = 0.0,
    this.visible = true,
    this.zIndex = 0.0,
    this.onTap,
  }) : assert(alpha == null || (0.0 <= alpha && alpha <= 1.0));

  /// Uniquely identifies a [Marker].
  final ClusterId markerId;

  @override
  ClusterId get mapsId => markerId;

  /// The label of the marker.
  final String? label;

  /// The opacity of the marker, between 0.0 and 1.0 inclusive.
  ///
  /// 0.0 means fully transparent, 1.0 means fully opaque.
  final double alpha;

  /// The icon image point that will be placed at the [position] of the marker.
  ///
  /// The image point is specified in normalized coordinates: An anchor of
  /// (0.0, 0.0) means the top left corner of the image. An anchor
  /// of (1.0, 1.0) means the bottom right corner of the image.
  final Offset anchor;

  /// True if the marker icon consumes tap events. If not, the map will perform
  /// default tap handling by centering the map on the marker and displaying its
  /// info window.
  final bool consumeTapEvents;

  /// True if the marker is draggable by user touch events.
  final bool draggable;

  /// True if the marker is rendered flatly against the surface of the Earth, so
  /// that it will rotate and tilt along with map camera movements.
  final bool flat;

  /// A description of the bitmap used to draw the marker icon.
  final BitmapDescriptor icon;

  /// A Google Maps InfoWindow.
  ///
  /// The window is displayed when the marker is tapped.
  final InfoWindow infoWindow;

  /// Geographical location of the marker.
  final LatLng position;

  /// Rotation of the marker image in degrees clockwise from the [anchor] point.
  final double rotation;

  /// True if the marker is visible.
  final bool visible;

  /// The z-index of the marker, used to determine relative drawing order of
  /// map overlays.
  ///
  /// Overlays are drawn in order of z-index, so that lower values means drawn
  /// earlier, and thus appearing to be closer to the surface of the Earth.
  final double zIndex;

  /// Callbacks to receive tap events for markers placed on this map.
  final VoidCallback? onTap;

  /// Creates a new [Marker] object whose values are the same as this instance,
  /// unless overwritten by the specified parameters.
  ClusterItem copyWith({
    double? alphaParam,
    String? labelParam,
    Offset? anchorParam,
    bool? consumeTapEventsParam,
    bool? draggableParam,
    bool? flatParam,
    BitmapDescriptor? iconParam,
    InfoWindow? infoWindowParam,
    LatLng? positionParam,
    double? rotationParam,
    bool? visibleParam,
    double? zIndexParam,
    VoidCallback? onTapParam,
  }) {
    return ClusterItem(
      markerId: markerId,
      alpha: alphaParam ?? alpha,
      label: labelParam ?? label,
      anchor: anchorParam ?? anchor,
      consumeTapEvents: consumeTapEventsParam ?? consumeTapEvents,
      draggable: draggableParam ?? draggable,
      flat: flatParam ?? flat,
      icon: iconParam ?? icon,
      infoWindow: infoWindowParam ?? infoWindow,
      position: positionParam ?? position,
      rotation: rotationParam ?? rotation,
      visible: visibleParam ?? visible,
      zIndex: zIndexParam ?? zIndex,
      onTap: onTapParam ?? onTap,
    );
  }

  /// Creates a new [Marker] object whose values are the same as this instance.
  ClusterItem clone() => copyWith();

  /// Converts this object to something serializable in JSON.
  Object toJson() {
    final Map<String, Object> json = <String, Object>{};

    void addIfPresent(String fieldName, Object? value) {
      if (value != null) {
        json[fieldName] = value;
      }
    }

    addIfPresent('markerId', markerId.value);
    addIfPresent('alpha', alpha);
    addIfPresent('label', label);
    addIfPresent('anchor', _offsetToJson(anchor));
    addIfPresent('consumeTapEvents', consumeTapEvents);
    addIfPresent('draggable', draggable);
    addIfPresent('flat', flat);
    addIfPresent('icon', icon.toJson());
    addIfPresent('infoWindow', infoWindow.toJson());
    addIfPresent('position', position.toJson());
    addIfPresent('rotation', rotation);
    addIfPresent('visible', visible);
    addIfPresent('zIndex', zIndex);
    return json;
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    if (other.runtimeType != runtimeType) return false;
    final ClusterItem typedOther = other as ClusterItem;
    return markerId == typedOther.markerId &&
        alpha == typedOther.alpha &&
        label == typedOther.label &&
        anchor == typedOther.anchor &&
        consumeTapEvents == typedOther.consumeTapEvents &&
        draggable == typedOther.draggable &&
        flat == typedOther.flat &&
        icon == typedOther.icon &&
        infoWindow == typedOther.infoWindow &&
        position == typedOther.position &&
        rotation == typedOther.rotation &&
        visible == typedOther.visible &&
        zIndex == typedOther.zIndex;
  }

  @override
  int get hashCode => markerId.hashCode;

  @override
  String toString() {
    return 'Marker{markerId: $markerId, alpha: $alpha, label: $label, anchor: $anchor, '
        'consumeTapEvents: $consumeTapEvents, draggable: $draggable, flat: $flat, '
        'icon: $icon, infoWindow: $infoWindow, position: $position, rotation: $rotation, '
        'visible: $visible, zIndex: $zIndex, onTap: $onTap}';
  }
}
