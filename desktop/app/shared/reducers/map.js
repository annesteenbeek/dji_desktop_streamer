// @flow
import { createReducer } from '@reduxjs/toolkit';

export const initialState = {
    showFlightPlan: false,
    map_center: {},
    name: '',
    polygon_border: [],
    flight_path: [],
    GSD: 2.00,
    nPhotos: 0,
    area: 0,
    settings: {
        uavType: 'mavicPro',
        side_overlay: 0.8,
        forward_overlay: 0.6,
        altitude: 30,
        speed: 0,
        start_point: 0,
        auto_heading: true,
        heading: 0,
    },
    UAVParams: {},
    UAVTypes: [],
    observables: {
        vehicle_positions: [],
        observation_positions: []
    },
}

const map = createReducer(initialState, {
    SET_POLYGON: (state, action) => {
        state.polygon_border = action.payload.borders
    },
    SET_FLIGHT_PATH: (state, action) => {
        state.flight_path = action.payload.path
        state.nPhotos = action.payload.nPhotos
    },
    INSERT_FLIGHT_PLAN: (state, action) => {
        state.showFlightPlan = true;
    },
    REMOVE_FLIGHT_PLAN: (state, action) => {
        state.showFlightPlan = false;
        state.polygon_border = [];
        state.flight_path = [];
        state.area = 0;
        state.nPhotos = 0;
        state.name = ''
    },
    SET_FLIGHT_SETTING: (state, action) => {
        state.settings[action.payload.setting] = action.payload.value;
    },
    SET_AREA: (state,action) => {
        state.area = action.payload.area
    },
    SET_GSD: (state, action) => {
        state.GSD = Number.parseFloat(action.payload.GSD).toPrecision(2)
    },
    SET_MISSION: (state, action) => {
        for (let [key, value] of Object.entries(action.payload.map)) {
            state[key] = value
        }
    },
    SET_MISSION_NAME: (state, action) => {
        state.name = action.payload.name
    },
    MAP_CENTER: (state, action) => {
        state.map_center = action.payload.center
    },
    SET_OBSERVATIONS: (state, action) => {
        state.observables = action.payload;
    }
 
})

export default map;
