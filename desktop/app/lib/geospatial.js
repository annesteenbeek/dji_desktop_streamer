import * as turf from "@turf/turf";
import uavParams from "./uavParams";

// Helper function to convert points to gmap points
function p2ll(point) {
  return { lng: point[0], lat: point[1] };
}

function ll2p(ll) {
  return [ll.lng, ll.lat];
}

function line2ll(line) {
  return line.map(point => p2ll(point));
}

function getLongestLine(polygon) {
  let maxDist = -Infinity;
  let line;
  turf.coordReduce(polygon, (prev, cur) => {
    let dist = turf.distance(prev, cur);
    if (dist > maxDist) {
      maxDist = dist;
      line = [prev, cur];
    }
    return cur;
  });
  return line;
}

function getGSD(settings, params) {
  // let params = uavParams[settings.uavType];
  // GSD [cm/pixel]
  console.log(params)
  let gsd = (params.sensor_size_mm[0]*settings.altitude*100)/(params.focal_length*params.image_size_px[0])
  console.log(gsd)
  return gsd
}

function getSpacing(settings, params) {
  // let params = uavParams[settings.uavType];
  
  let sampling_d = settings.altitude/params.focal_length; // [m/mm]
  let footprint_width = params.sensor_size_mm[0] * sampling_d; // [m]
  let footprint_height = params.sensor_size_mm[1] * sampling_d;
  
  let side_spacing = (1-settings.side_overlay) * footprint_width;
  let forward_spacing = (1-settings.forward_overlay) * footprint_height;
  
  return [side_spacing, forward_spacing] // in meters
}

function flightGrid(outline, settings, params) {
  
  let [side_spacing, forward_spacing] = getSpacing(settings, params);
  
  let spacing_rad = turf.lengthToDegrees(side_spacing, 'meters');
  let forward_rad = turf.lengthToDegrees(forward_spacing, 'meters');
  let poly = outlineToPoly(outline);
  
  let side = getLongestLine(poly);
  let heading = turf.bearing(side[0], side[1]);
  let pivot = side[0]; // pivot point
  
  // rotate coordinate frame to simplify calculations
  poly = turf.transformRotate(poly, -heading, { pivot: pivot });
  let bbox = turf.bbox(poly);
  let [minE, minN, maxE, maxN] = bbox;
  
  let fit = (maxE - minE) / spacing_rad;
  let n_lines = Math.floor(fit);
  let offset = ((fit - n_lines) * spacing_rad) / 2; // left at side
  
  // Mask with polygon
  let lines = [];
  let n_images = 0;
  let dir = true;
  for (let n = 0; n <= n_lines; n++) {
    let lng = minE + offset + n * spacing_rad;
    let line = turf.lineString([[lng, minN], [lng, maxN]]);
    let intersects = turf.lineIntersect(line, poly);
    
    let n_features = intersects.features.length;
    console.assert(
      n_features > 1,
      "Unnacounted number of intersects: " + n_features
    );
      
    let _maxN = -Infinity;
    let _minN = Infinity;
    turf.coordEach(intersects, point => {
      _maxN = Math.max(point[1], _maxN);
      _minN = Math.min(point[1], _minN);
    });
    
    n_images = n_images + Math.floor((_maxN-_minN)/forward_rad)
    
    let p1 = [lng, _minN];
    let p2 = [lng, _maxN];
    
    dir ? lines.push(p1, p2) : lines.push(p2, p1);
    dir = !dir;
  }
    
  lines = turf.transformRotate(turf.lineString(lines), heading, {
    pivot: pivot
  });
    
  return [line2ll(turf.getCoords(lines)), n_images]
}
  
function outlineToPoly(outline) {
  var polyPoints = outline.map(point => ll2p(point));
  polyPoints.push(polyPoints[0]);
  return turf.polygon([polyPoints]);
}

function getArea(outline) {
  const polygon = outlineToPoly(outline);
  return turf.area(polygon);
}

function getCenter(outline) {
  const polygon = outlineToPoly(outline);
  return p2ll(turf.getCoords(turf.center(polygon)))
}

function getBounds(outline) {
  let polygon = outlineToPoly(outline)
  let bbox = turf.bbox(polygon);
  let [minE, minN, maxE, maxN] = bbox;
  
  return {east: maxE, north: maxN, south: minN, west: minE}
}


function flightplanToJson(path, settings, params, name) {
  let [, forward_spacing] = getSpacing(settings, params);
  let photoPoint = true
  
  let waypointMission = {
    mission_type: "NADIR",
    mission_name: name,
    autoFlightSpeed: settings.speed,
    waypoints: path.map((point)=>{
      let waypoint = {
        altitude: settings.altitude,
        latitude: point.lat,
        longitude: point.lng,
        gimbalPitch: -90,
        shootPhotoDistanceInterval: photoPoint ? forward_spacing : 0,
      }
      photoPoint = !photoPoint
      
      return waypoint
    }),
  }
  return waypointMission
}

function observationsToJson(observation_positions, settings) {
  let waypointMission = {
    mission_type: "ALPR",
    mission_name: 'ALPR',
    autoFlightSpeed: settings.speed,
    waypoints: observation_positions.filter((point) => {
      if (point.lat == 0) {
        return false
      } else {
        return true
      }
    }).map((point)=>{
      let waypoint = {
        altitude: point.alt,
        latitude: point.lat,
        longitude: point.lng,
        yaw: point.yaw,
        gimbalPitch: point.camera_pitch,
      }
      return waypoint
    }),
  }
  return waypointMission
}

module.exports = {
  flightGrid,
  getArea,
  getGSD,
  flightplanToJson,
  observationsToJson,
  getCenter,
  getBounds,
}
