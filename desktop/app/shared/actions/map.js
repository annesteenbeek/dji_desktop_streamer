import geo from '../../lib/geospatial';
import { createAliasedAction } from 'electron-redux';
import { determine_observations } from '../../src/ExternalHandler';

export const setPolygonPath = (borders) => ({
    type: 'SET_POLYGON',
    payload: {
        borders
    },
    meta: {
        scope: 'local'
    }
})

export const setFlightPath = (path, nPhotos) => ({
    type: 'SET_FLIGHT_PATH',
    payload: {
        path,
        nPhotos
    },
    meta: {
        scope: 'local'
    }
})

export const setGSD = (GSD) => ({
    type: 'SET_GSD',
    payload: {
        GSD
    },
    meta: {
        scope:'local'
    }
})

export const setArea = (area) => ({
    type: 'SET_AREA',
    payload: {
        area
    },
    meta: {
        scope: 'local'
    }
})

export const insertFlightPlan = () => ({
    type: 'INSERT_FLIGHT_PLAN',
    meta: {
        scope: 'local'
    }
})

export const removeFlightPlan = () => ({
    type: 'REMOVE_FLIGHT_PLAN',
    meta: {
        scope: 'local'
    }
})

export const setFlightSetting = (setting, value) => ({
    type: 'SET_FLIGHT_SETTING',
    payload: {
        setting,
        value
    },
    meta: {
        scope: 'local'
    }
})

export const setMissionName= (name) => ({
    type: 'SET_MISSION_NAME',
    payload: {
        name
    },
    meta: {
        scope: 'local'
    }
})

export const setMission = (map) => ({
    type: 'SET_MISSION',
    payload: {
        map
    },
    meta: {
        scope: 'local'
    }
})

export const currentMapCenter = (center) => ({
    type: 'MAP_CENTER',
    payload: {
        center
    },
    meta: {
        scope: 'local'
    }
})

// THUNKS
// sets the outline of thep olygon and determines new flight path
export function setPolygonPathThunk(borders) {
    return (dispatch, getState) => {
        let area = geo.getArea(borders);
        dispatch(setArea(area));
        dispatch(setPolygonPath(borders))

        return dispatch(calculateFlightPathThunk())
    }
}

export function calculateFlightPathThunk() {
    return (dispatch, getState) => {
        let settings = getState().map.settings;
        let params = getState().map.UAVParams[settings.uavType]
        let borders = getState().map.polygon_border;

        return dispatch(setFlightPath(...geo.flightGrid(borders, settings, params)))
    }
} 

export function setFlightSettingThunk(setting, value) {

    return (dispatch, getState) => {
        dispatch(setFlightSetting(setting, value))
        if (setting === 'altitude') {
            let settings = getState().map.settings
            let params = getState().map.UAVParams[settings.uavType]
            dispatch(setGSD(geo.getGSD(settings, params)))
        }
        if (getState().map.showFlightPlan) {
            dispatch(calculateFlightPathThunk())
        }
   }
}

export function insertFlightPlanThunk() {
    return (dispatch, getState) => {
        let map_center = getState().map.map_center;
        
        let cLat = map_center.lat;
        let cLng = map_center.lng;
        let size = 0.0005;
        let initialCoords = [
            {lat: cLat+size, lng: cLng-size},
            {lat: cLat+size, lng: cLng+size},
            {lat: cLat-size, lng: cLng+size},
            {lat: cLat-size, lng: cLng-size}
        ]
        dispatch(setPolygonPathThunk(initialCoords))
        return dispatch(insertFlightPlan())
    }
}

export const mainSetObservations = createAliasedAction(
    'SET_OBSERVATIONS',
    () => {
        let vehicle_positions = [];
        let observation_positions = [];
        try {
            let locations = determine_observations();
            vehicle_positions = locations[0];
            observation_positions = locations[1];
        } catch(err) {
            console.log(err)
        }
        return {
            type: 'SET_OBSERVATIONS',
            payload: {
                vehicle_positions,
                observation_positions

            }
        }
    }
)

