// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import "GoogleMapClusterController.h"
#import "JsonConversions.h"
@import GoogleMapsUtils;


static UIImage* ExtractIcon(NSObject<FlutterPluginRegistrar>* registrar, NSArray* icon);
static void InterpretInfoWindow(id<FLTGoogleMapClusterOptionsSink> sink, NSDictionary* data);

@implementation FLTGoogleMapClusterController {
    GMSMarker* _marker;
    GMSMapView* _mapView;
    BOOL _consumeTapEvents;
    NSString* _label;
}
- (instancetype)initMarkerWithPosition:(CLLocationCoordinate2D)position
                              markerId:(NSString*)markerId
                               mapView:(GMSMapView*)mapView {
    self = [super init];
    if (self) {
        _marker = [GMSMarker markerWithPosition:position];
        _mapView = mapView;
        _markerId = markerId;
        _marker.userData = @[ _markerId ];
        _consumeTapEvents = NO;
    }
    return self;
}
- (void)showInfoWindow {
    _mapView.selectedMarker = _marker;
}
- (void)hideInfoWindow {
    if (_mapView.selectedMarker == _marker) {
        _mapView.selectedMarker = nil;
    }
}
- (BOOL)isInfoWindowShown {
    return _mapView.selectedMarker == _marker;
}
- (BOOL)consumeTapEvents {
    return _consumeTapEvents;
}
- (NSString*)label {
    return _label;
}
- (void)removeMarker {
    _marker.map = nil;
}
- (GMSMarker*)getMarker {
    return _marker;
}
#pragma mark - FLTGoogleMapClusterOptionsSink methods
- (void)setLabel:(NSString*)label {
    _label = label;
}
- (void)setAlpha:(float)alpha {
    _marker.opacity = alpha;
}
- (void)setAnchor:(CGPoint)anchor {
    _marker.groundAnchor = anchor;
}
- (void)setConsumeTapEvents:(BOOL)consumes {
    _consumeTapEvents = consumes;
}
- (void)setDraggable:(BOOL)draggable {
    _marker.draggable = draggable;
}
- (void)setFlat:(BOOL)flat {
    _marker.flat = flat;
}
- (void)setIcon:(UIImage*)icon {
    _marker.icon = icon;
}
- (void)setInfoWindowAnchor:(CGPoint)anchor {
    _marker.infoWindowAnchor = anchor;
}
- (void)setInfoWindowTitle:(NSString*)title snippet:(NSString*)snippet {
    _marker.title = title;
    _marker.snippet = snippet;
}
- (void)setPosition:(CLLocationCoordinate2D)position {
    _marker.position = position;
}
- (void)setRotation:(CLLocationDegrees)rotation {
    _marker.rotation = rotation;
}
- (void)setZIndex:(int)zIndex {
    _marker.zIndex = zIndex;
}
@end

static double ToDouble(NSNumber* data) { return [FLTGoogleMapJsonConversions toDouble:data]; }

static float ToFloat(NSNumber* data) { return [FLTGoogleMapJsonConversions toFloat:data]; }

static CLLocationCoordinate2D ToLocation(NSArray* data) {
    return [FLTGoogleMapJsonConversions toLocation:data];
}

static int ToInt(NSNumber* data) { return [FLTGoogleMapJsonConversions toInt:data]; }

static BOOL ToBool(NSNumber* data) { return [FLTGoogleMapJsonConversions toBool:data]; }

static CGPoint ToPoint(NSArray* data) { return [FLTGoogleMapJsonConversions toPoint:data]; }

static NSArray* PositionToJson(CLLocationCoordinate2D data) {
    return [FLTGoogleMapJsonConversions positionToJson:data];
}

static void InterpretClusterOptions(NSDictionary* data, id<FLTGoogleMapClusterOptionsSink> sink,
                                    NSObject<FlutterPluginRegistrar>* registrar) {
    NSNumber* alpha = data[@"alpha"];
    if (alpha != nil) {
        [sink setAlpha:ToFloat(alpha)];
    }
    NSArray* anchor = data[@"anchor"];
    if (anchor) {
        [sink setAnchor:ToPoint(anchor)];
    }
    NSNumber* draggable = data[@"draggable"];
    if (draggable != nil) {
        [sink setDraggable:ToBool(draggable)];
    }
    NSString* label = data[@"label"];
    if (label != nil) {
        [sink setLabel:label];
    }
    NSArray* icon = data[@"icon"];
    if (icon) {
        UIImage* image = ExtractIcon(registrar, icon);
        [sink setIcon:image];
    }
    NSNumber* flat = data[@"flat"];
    if (flat != nil) {
        [sink setFlat:ToBool(flat)];
    }
    NSNumber* consumeTapEvents = data[@"consumeTapEvents"];
    if (consumeTapEvents != nil) {
        [sink setConsumeTapEvents:ToBool(consumeTapEvents)];
    }
    InterpretInfoWindow(sink, data);
    NSArray* position = data[@"position"];
    if (position) {
        [sink setPosition:ToLocation(position)];
    }
    NSNumber* rotation = data[@"rotation"];
    if (rotation != nil) {
        [sink setRotation:ToDouble(rotation)];
    }
    NSNumber* zIndex = data[@"zIndex"];
    if (zIndex != nil) {
        [sink setZIndex:ToInt(zIndex)];
    }
}

static void InterpretInfoWindow(id<FLTGoogleMapClusterOptionsSink> sink, NSDictionary* data) {
    NSDictionary* infoWindow = data[@"infoWindow"];
    if (infoWindow) {
        NSString* title = infoWindow[@"title"];
        NSString* snippet = infoWindow[@"snippet"];
        if (title) {
            [sink setInfoWindowTitle:title snippet:snippet];
        }
        NSArray* infoWindowAnchor = infoWindow[@"infoWindowAnchor"];
        if (infoWindowAnchor) {
            [sink setInfoWindowAnchor:ToPoint(infoWindowAnchor)];
        }
    }
}

static UIImage* scaleImage(UIImage* image, NSNumber* scaleParam) {
    double scale = 1.0;
    if ([scaleParam isKindOfClass:[NSNumber class]]) {
        scale = scaleParam.doubleValue;
    }
    if (fabs(scale - 1) > 1e-3) {
        return [UIImage imageWithCGImage:[image CGImage]
                                   scale:(image.scale * scale)
                             orientation:(image.imageOrientation)];
    }
    return image;
}

static UIImage* ExtractIcon(NSObject<FlutterPluginRegistrar>* registrar, NSArray* iconData) {
    UIImage* image;
    if ([iconData.firstObject isEqualToString:@"defaultMarker"]) {
        CGFloat hue = (iconData.count == 1) ? 0.0f : ToDouble(iconData[1]);
        image = [GMSMarker markerImageWithColor:[UIColor colorWithHue:hue / 360.0
                                                           saturation:1.0
                                                           brightness:0.7
                                                                alpha:1.0]];
    } else if ([iconData.firstObject isEqualToString:@"fromAsset"]) {
        if (iconData.count == 2) {
            image = [UIImage imageNamed:[registrar lookupKeyForAsset:iconData[1]]];
        } else {
            image = [UIImage imageNamed:[registrar lookupKeyForAsset:iconData[1]
                                                         fromPackage:iconData[2]]];
        }
    } else if ([iconData.firstObject isEqualToString:@"fromAssetImage"]) {
        if (iconData.count == 3) {
            image = [UIImage imageNamed:[registrar lookupKeyForAsset:iconData[1]]];
            NSNumber* scaleParam = iconData[2];
            image = scaleImage(image, scaleParam);
        } else {
            NSString* error =
            [NSString stringWithFormat:@"'fromAssetImage' should have exactly 3 arguments. Got: %lu",
             (unsigned long)iconData.count];
            NSException* exception = [NSException exceptionWithName:@"InvalidBitmapDescriptor"
                                                             reason:error
                                                           userInfo:nil];
            @throw exception;
        }
    } else if ([iconData[0] isEqualToString:@"fromBytes"]) {
        if (iconData.count == 2) {
            @try {
                FlutterStandardTypedData* byteData = iconData[1];
                CGFloat screenScale = [[UIScreen mainScreen] scale];
                image = [UIImage imageWithData:[byteData data] scale:screenScale];
            } @catch (NSException* exception) {
                @throw [NSException exceptionWithName:@"InvalidByteDescriptor"
                                               reason:@"Unable to interpret bytes as a valid image."
                                             userInfo:nil];
            }
        } else {
            NSString* error = [NSString
                               stringWithFormat:@"fromBytes should have exactly one argument, the bytes. Got: %lu",
                               (unsigned long)iconData.count];
            NSException* exception = [NSException exceptionWithName:@"InvalidByteDescriptor"
                                                             reason:error
                                                           userInfo:nil];
            @throw exception;
        }
    }
    
    return image;
}

@implementation FLTClustersController {
    NSMutableDictionary* _clusterIdToController;
    FlutterMethodChannel* _methodChannel;
    NSObject<FlutterPluginRegistrar>* _registrar;
    GMSMapView* _mapView;
    GMUClusterManager *_clusterManager;
}
- (instancetype)init:(FlutterMethodChannel*)methodChannel
             mapView:(GMSMapView*)mapView
      clusterManager:(GMUClusterManager*)clusterManager
           registrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    self = [super init];
    if (self) {
        _methodChannel = methodChannel;
        _mapView = mapView;
        _clusterManager = clusterManager;
        _clusterIdToController = [NSMutableDictionary dictionaryWithCapacity:1];
        _registrar = registrar;
        
    }
    return self;
}

- (void)addClusterItems:(NSArray*)addClusterItems {
    for (NSDictionary* marker in addClusterItems) {

      CLLocationCoordinate2D position = [FLTClustersController getPosition:marker];
      NSString* markerId = [FLTClustersController getMarkerId:marker];
      FLTGoogleMapClusterController* controller =
      [[FLTGoogleMapClusterController alloc] initMarkerWithPosition:position
                                                           markerId:markerId
                                                           mapView:_mapView];

      InterpretClusterOptions(marker, controller, _registrar);
      GMSMarker *marker = controller.getMarker;
      _clusterIdToController[markerId] = controller;

      if ([controller.label isEqual: NULL] || [controller.label isEqual: @""])
      {
        [_clusterManager addItem:marker];
      } else {
        UIImage *markerIcon = marker.icon;
        UIView *mainView = [[UIView alloc] init];

        UIImageView *imageView = [[UIImageView alloc] initWithImage:markerIcon];
        imageView.contentMode = UIViewContentModeScaleAspectFit;
        imageView.frame = CGRectMake(0, 0, markerIcon.size.width+2, markerIcon.size.height+2);
        imageView.contentMode = UIViewContentModeScaleAspectFill;


        UITextView *myTextView = [[UITextView alloc] init];
        myTextView.textContainerInset = UIEdgeInsetsMake(8, 3, 8, 3);
        myTextView.text = controller.label;
        myTextView.backgroundColor = [UIColor colorWithRed:76/255.0
                                                       green:184/255.0
                                                       blue:172/255.0
                                                       alpha:1];
        [myTextView setTextColor: [UIColor whiteColor]];
        [myTextView setFont:[UIFont boldSystemFontOfSize:14]];
        myTextView.layer.cornerRadius = 4;
        [myTextView sizeToFit];

        [mainView setFrame:CGRectMake(0,0,myTextView.frame.size.width,imageView.frame.size.height+myTextView.frame.size.height+2)];

        UIView *roundTextView = [[UIView alloc] initWithFrame:CGRectMake(mainView.frame.size.width/2.9,0,imageView.frame.size.width,imageView.frame.size.height)];
        roundTextView.backgroundColor = [UIColor clearColor];
        [self setMaskTo:roundTextView byRoundingCorners:UIRectCornerAllCorners];
        [roundTextView addSubview: imageView];
        roundTextView.clipsToBounds = true;

        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0,imageView.frame.size.height+2,myTextView.frame.size.width,myTextView.frame.size.height)];
        [view addSubview:myTextView];

        view.backgroundColor = [UIColor colorWithRed:76/255.0
                                                       green:184/255.0
                                                        blue:172/255.0
                                                       alpha:1];
        view.layer.cornerRadius = 4;

        UIStackView *stackView = [[UIStackView alloc] init];
        stackView.axis = UILayoutConstraintAxisVertical;
        stackView.distribution = UIStackViewDistributionEqualSpacing;
        stackView.alignment = UIStackViewAlignmentCenter;
        stackView.spacing = 0;

        [stackView addArrangedSubview:roundTextView];
        [stackView addArrangedSubview:view];

        stackView.translatesAutoresizingMaskIntoConstraints = false;
        [mainView addSubview:stackView];

        UIImage *markerIcon1 = [self imageFromView:mainView];
        marker.icon = markerIcon1;
        [_clusterManager addItem:marker];

      }
    }
}


- (void)removeClusterIds:(NSArray*)clusterIdsToRemove {
    for (NSString* markerId in clusterIdsToRemove) {
      if (!markerId) {
          continue;
      }
      FLTGoogleMapClusterController* controller = _clusterIdToController[markerId];
      if (!controller) {
          continue;
      }
      GMSMarker *marker = controller.getMarker;
      [_clusterManager removeItem:marker];
      [controller removeMarker];
      [_clusterIdToController removeObjectForKey:markerId];
    }
}

- (id<GMUClusterIconGenerator>)defaultIconGenerator {
    return [[GMUDefaultClusterIconGenerator alloc] init];
}
typedef NS_ENUM(NSInteger, ClusterAlgorithmMode) {
    kClusterAlgorithmGridBased,
    kClusterAlgorithmQuadTreeBased,
};
- (id<GMUClusterAlgorithm>)algorithmForMode:(ClusterAlgorithmMode)mode {
    switch (mode) {
        case kClusterAlgorithmGridBased:
            return [[GMUGridBasedClusterAlgorithm alloc] init];
            
        case kClusterAlgorithmQuadTreeBased:
            return [[GMUNonHierarchicalDistanceBasedAlgorithm alloc] init];
            
        default:
            assert(false);
            break;
    }
}
- (UIImage *)imageFromView:(UIView *) view
{
    if ([[UIScreen mainScreen] respondsToSelector:@selector(scale)]) {
        UIGraphicsBeginImageContextWithOptions(view.frame.size, NO, [[UIScreen mainScreen] scale]);
    } else {
        UIGraphicsBeginImageContext(view.frame.size);
    }
    [view.layer renderInContext: UIGraphicsGetCurrentContext()];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

- (void)setMaskTo:(UIView*)view byRoundingCorners:(UIRectCorner)corners
{
    UIBezierPath *rounded = [UIBezierPath bezierPathWithRoundedRect:view.bounds
                                                  byRoundingCorners:corners
                                                        cornerRadii:CGSizeMake(20, 20)];
    CAShapeLayer *shape = [[CAShapeLayer alloc] init];
    [shape setPath:rounded.CGPath];
    view.layer.mask = shape;
    
}
- (BOOL)onClusterItemTap:(NSString*)markerId {
    if (!markerId) {
        return NO;
    }
    FLTGoogleMapMarkerController* controller = _clusterIdToController[markerId];
    if (!controller) {
        return NO;
    }
    [_methodChannel invokeMethod:@"clusterItem#onTap" arguments:@{@"markerId" : markerId}];
    return controller.consumeTapEvents;
}
- (BOOL)onClusterTap:(id<GMUCluster>)cluster {
    GMSCoordinateBounds *bounds = [[GMSCoordinateBounds alloc] init];
    CLLocationCoordinate2D location;
    
    for (id<GMUClusterItem> item in cluster.items) {
        
        bounds = [bounds includingCoordinate:item.position];
        location.latitude = item.position.latitude;
        location.latitude = item.position.longitude;
        
    }
    
    [CATransaction begin];
    [CATransaction setValue:[NSNumber numberWithFloat: 0.3f] forKey:kCATransactionAnimationDuration];
    [CATransaction setCompletionBlock:^{
    }];
    GMSCameraUpdate *update = [GMSCameraUpdate fitBounds:bounds withPadding:50.0f];
    
    [_mapView animateWithCameraUpdate:update];
    [CATransaction commit];
    
    return YES;
}
+ (CLLocationCoordinate2D)getPosition:(NSDictionary*)marker {
    NSArray* position = marker[@"position"];
    return ToLocation(position);
}
+ (NSString*)getMarkerId:(NSDictionary*)marker {
    return marker[@"markerId"];
}
@end
