// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import <Flutter/Flutter.h>
#import <GoogleMaps/GoogleMaps.h>
#import "GoogleMapController.h"
#import "GoogleMapMarkerController.h"

@import GoogleMapsUtils;

NS_ASSUME_NONNULL_BEGIN

// Defines cluster UI options writable from Flutter.
@protocol FLTGoogleMapClusterOptionsSink
- (void)setAlpha:(float)alpha;
- (void)setAnchor:(CGPoint)anchor;
- (void)setConsumeTapEvents:(BOOL)consume;
- (void)setLabel:(NSString*)label;
- (void)setDraggable:(BOOL)draggable;
- (void)setFlat:(BOOL)flat;
- (void)setIcon:(UIImage*)icon;
- (void)setInfoWindowAnchor:(CGPoint)anchor;
- (void)setInfoWindowTitle:(NSString*)title snippet:(NSString*)snippet;
- (void)setPosition:(CLLocationCoordinate2D)position;
- (void)setRotation:(CLLocationDegrees)rotation;
//- (void)setVisible:(BOOL)visible;
- (void)setZIndex:(int)zIndex;
@end

// Defines marker controllable by Flutter.
@interface FLTGoogleMapClusterController : NSObject <FLTGoogleMapClusterOptionsSink>
@property(atomic, readonly) NSString* markerId;
- (instancetype)initMarkerWithPosition:(CLLocationCoordinate2D)position
                              markerId:(NSString*)markerId
                               mapView:(GMSMapView*)mapView;
- (void)showInfoWindow;
- (void)hideInfoWindow;
- (BOOL)isInfoWindowShown;
- (BOOL)consumeTapEvents;
- (NSString*)label;
- (void)removeMarker;
- (GMSMarker*)getMarker;

@end

@interface FLTClustersController : NSObject
- (instancetype)init:(FlutterMethodChannel*)methodChannel
             mapView:(GMSMapView*)mapView
                   clusterManager:(GMUClusterManager*)clusterManager

           registrar:(NSObject<FlutterPluginRegistrar>*)registrar;
- (void)addClusterItems:(NSArray*)addClusterItems;
- (BOOL)onClusterTap:(id<GMUCluster>)cluster;
- (BOOL)onClusterItemTap:(NSString*)markerId;
- (void)removeClusterIds:(NSArray*)clusterIdsToRemove;

@end

NS_ASSUME_NONNULL_END
