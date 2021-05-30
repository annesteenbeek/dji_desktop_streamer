// @flow

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { GoogleMap, LoadScript, Polygon, StandaloneSearchBox } from '@react-google-maps/api'
import { Container } from 'semantic-ui-react';
import FlightArea from '../molecules/FlightArea';
import SearchBox from '../atoms/SearchBox';
import { insertFlightPlan, removeFlightPlan, currentMapCenter } from '../../shared/actions/index';

const { GOOGLE_MAPS_API_KEY } = require('../../conf');
const libraries = ['places'];
const searchBounds = {west: 3.31497114423, 
                      south: 50.803721015, 
                      east: 7.09205325687, 
                      north: 53.5104033474}; // search bounds NL

class MapContainer extends Component<Props> {
  
  constructor(props) {
    super(props);
    this.state = {
      map: undefined,
      loaded: false,
      center: { lat: 52.213, lng: 6.899 } // default center
   };
    
    var options = {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    };
    
    navigator.geolocation.getCurrentPosition((pos) => {
      // on success
      const crd = pos.coords;
      this.setState({center: {lat: crd.latitude, lng: crd.longitude}})
    }, (err)=>{
      console.warn(`ERROR(${err.code}): ${err.message}`);
    }, options);
    
  }
  
  onMapLoad = (ref) => {
    this.setState({map: ref}, () => {
      this.newCenter();
    });
  }

  onSearchboxLoad = (ref) => {
    this.setState({search: ref})
  }

  onPlacesChanged = () => {
    let place = this.state.search.getPlaces()[0];
    this.state.map.fitBounds(place.geometry.viewport)
  }

  newCenter = () => {
    if (typeof this.state.map !== 'undefined') {
      let center = this.state.map.getCenter()
      this.props.setCurrentMapCenter({lat: center.lat(), lng: center.lng()})
    }
  }

  render() {
    return (
      <Container style={{ height: '100vh' }}>
        <LoadScript
        id="script-loader"
        libraries={libraries}
        googleMapsApiKey={GOOGLE_MAPS_API_KEY}
        language="nl"
        region="nl"
        onLoad={()=>{
          this.setState({loaded: true})
        }}
        >
          {this.state.loaded &&
            <GoogleMap
              mapContainerStyle={{height: '100%', width: '100%'}}
              options={{ 
                clickableIcons: false,
                streetViewControl: false,
                fullscreenControl: false,
                gestureHandling: 'greedy',
                mapTypeControl: true,
                mapTypeId: window.google.maps.MapTypeId.SATELLITE,
                mapTypeControlOptions: {
                  style: window.google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
                  position: window.google.maps.ControlPosition.TOP_RIGHT,
                  mapTypeIds: [
                    window.google.maps.MapTypeId.ROADMAP,
                    window.google.maps.MapTypeId.SATELLITE,
                    window.google.maps.MapTypeId.HYBRID
                  ]
                }
              }}
              center={this.state.center}
              onDragEnd={this.newCenter}
              onCenterChanged={this.newCenter}
              zoom={12}
              version="weekly"
              onLoad={this.onMapLoad}
              >
            <StandaloneSearchBox 
              onLoad={this.onSearchboxLoad} 
              onPlacesChanged={this.onPlacesChanged}
              bounds={searchBounds} 
            >
              <SearchBox/>
           </StandaloneSearchBox>
            { this.props.showFlightPlan && 
              this.props.polygon_path &&
              this.state.map &&
              <FlightArea
              map={this.state.map}/>
            }
        </GoogleMap>
        }
      </LoadScript>
    </Container>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    showFlightPlan: state.map.showFlightPlan,
    polygon_path: state.map.polygon_border,
  };
};

const mapDispatchToProps = (dispatch) => ({
  insertFlightPlan: () => dispatch(insertFlightPlan()),
  removeFlightPlan: () => dispatch(removeFlightPlan()),
  setCurrentMapCenter: (center) => dispatch(currentMapCenter(center))
});


export default connect(mapStateToProps, mapDispatchToProps)(MapContainer);
