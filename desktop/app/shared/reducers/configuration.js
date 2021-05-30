// @flow
import { createReducer } from '@reduxjs/toolkit';

export const initialState = {
    storage_location: '',
    mission_list: [],
    network_info: {
        address: '',
        port: '' 
    }
}

const configuration = createReducer(initialState, {
    SET_STORAGE_LOCATION: (state, action) => {
        if (typeof action.payload.storage_location !== 'undefined') {
            state.storage_location = action.payload.storage_location;
        }
    },
    SET_NETWORK_INFO: (state, action) => {
        state.network_info = {address: action.payload.address, port: action.payload.port}
    },
    SET_MISSION_LIST: (state, action) => {
        state.mission_list = action.payload.missions
    }
})

export default configuration;
