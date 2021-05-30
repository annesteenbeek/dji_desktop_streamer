// @flow

import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Polygon, Polyline, Marker } from '@react-google-maps/api';
// import { setPolygonPath, setFlightPath } from '../../actions/index';
import { setPolygonPathThunk } from '../../shared/actions/index';
import { arePathsEqual } from '../../lib/arePathsEqual';
import { getCenter, getBounds } from '../../lib/geospatial';
import fontawesome from 'fontawesome-markers';


class FlightArea extends Component<Props> {
  
  constructor(props) {
    super(props);
    this.state = {
      polygon: undefined,
    }
  }

  componentDidMount() {

    // this.props.map.setZoom(17);
    this.props.map.fitBounds(getBounds(this.props.polygon_path))

    // this.props.map.setCenter(getCenter(this.props.polygon_path))
  }

 onLoad = (polygon) => {
    this.setState({polygon: polygon}, ()=> {
      // this.onPathChange();
    })
  }
  
  onEdit = (e) => {
    this.onPathChange();
  }
  
  onPathChange() {
    if (typeof this.state.polygon !== 'undefined') {
      const path = this.state.polygon.getPath().getArray().map(p=>{
        return {lat: p.lat(), lng: p.lng()}
      })
      if (! arePathsEqual(path, this.props.polygon_path)) {
        // TODO: drag and mouseup are both being registered
        this.props.setPolygonPath(path)
      }
    }
  }

  render() {
    const corner_icon = {
                  path: fontawesome.CIRCLE,
                  scale: 0.15,
                  anchor: new google.maps.Point(22, -22),
                 // strokeWeight: 0.2,
                  strokeColor: '#5d77de',
                  strokeOpacity: 1,
                  fillColor: '#5d77de',
                  fillOpacity: 1.0,
                  }
    const vehicle_icon = {
                  path: fontawesome.CAR,
                  scale: 0.15,
                  anchor: new google.maps.Point(22, -22),
                 // strokeWeight: 0.2,
                  strokeColor: '#10de1a',
                  strokeOpacity: 1,
                  fillColor: '#10de1a',
                  fillOpacity: 1.0,
                  }
    const observation_icon = {
                  path: fontawesome.CAMERA,
                  scale: 0.15,
                  anchor: new google.maps.Point(22, -22),
                 // strokeWeight: 0.2,
                  strokeColor: '#e31033',
                  strokeOpacity: 1,
                  fillColor: '#e31033',
                  fillOpacity: 1.0,
                  }
    
    return <div>
    <Polyline
    click
    options={{
      clickable: false,
      path: this.props.flight_path,
      strokeColor: '#51bddb',
      strokeOpacity: 1.0,
      strokeWeight: 2,
      geodesic: true
    }}
    />

    {this.props.flight_path.map((point, index) => {
        return <Marker
                clickable={false}
                key={index}
                icon={corner_icon}
                // icon={url_icon}
                position={point}
                title='photo'
                />
    })}

    {/* Vehicles  */}
    {this.props.vehicle_positions.map((point, index) => {
        return <Marker
                clickable={false}
                key={index}
                icon={vehicle_icon}
                position={point}
                title='vehicle'
                />
    })}

    {/* Observation points */}
    {this.props.observation_positions.map((point, index) => {
        if (point.lat == 0) {
          return
        }
        return <Marker
                clickable={false}
                key={index}
                icon={observation_icon}
                position={point}
                title='observation point'
                />
    })}

   <Polygon
    options={{
      strokeColor:"#3556db",
      strokeOpacity:0.8,
      strokeWeight:3,
      fillColor:"#5d77de",
      fillOpacity:0.15,
    }}
    editable
    draggable
    path={this.props.polygon_path}
    onMouseUp={this.onEdit}
    onDragEnd={this.onEdit}
    onLoad={this.onLoad}
    // onUnmount={onUnmount}
    />
    </div>
  }
}

FlightArea.propTypes = {
  map: PropTypes.object,
  initialCenter: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.shape({
      lat: PropTypes.number,
      lng: PropTypes.number,
    }),
  ]),
}

const mapStateToProps = (state) => {
  return {
    polygon_path: state.map.polygon_border,
    flight_path: state.map.flight_path,
    vehicle_positions: state.map.observables.vehicle_positions,
    observation_positions: state.map.observables.observation_positions
  }
}

const mapDispatchToProps = (dispatch) => ({
  setPolygonPath: (path) => { dispatch(setPolygonPathThunk(path))},
})


export default connect(mapStateToProps, mapDispatchToProps)(FlightArea);
