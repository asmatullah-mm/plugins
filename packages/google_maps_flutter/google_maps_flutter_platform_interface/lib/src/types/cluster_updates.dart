// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'types.dart';

/// [ClusterItem] update events to be applied to the [GoogleMap].
///
/// Used in [GoogleMapController] when the map is updated.
// (Do not re-export)
class ClusterUpdates extends MapsObjectUpdates<ClusterItem> {
  /// Computes [ClusterUpdates] given previous and current [ClusterItem]s.
  ClusterUpdates.from(Set<ClusterItem> previous, Set<ClusterItem> current)
      : super.from(previous, current, objectName: 'clusterItem');

  /// Set of Markers to be added in this update.
  Set<ClusterItem> get clusterItemsToAdd => objectsToAdd;

  /// Set of MarkerIds to be removed in this update.
  Set<MarkerId> get clusterItemsToChange => objectIdsToRemove.cast<MarkerId>();

  /// Set of Markers to be changed in this update.
  Set<ClusterItem> get clusterItemIdsToRemove => objectsToChange;
}
