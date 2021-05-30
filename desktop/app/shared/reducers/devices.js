// @flow
import { createReducer } from '@reduxjs/toolkit';

const initialState = {
    socketConnected: false,
    product: {
        connected: false,
        model: '',
        modelName: '',
        djiLoggedin: false,
        sdkRegistered: false,
        remoteConnected: false,
        flightControllerConnected: false
    },
    mission: {
        state: '',
    }
}

export default createReducer(initialState, {
    SET_CONNECTED: (state, action) => {
        let connected = action.payload.connected;
        state.socketConnected = connected
        if (!connected) {
            state.product = initialState.product;
        }

    },
    SET_PRODUCT_STATE: (state, action) => {
        state.product = action.payload.product
    },
    SET_MISSION_STATE: (state, action) => {
        state.mission = action.payload.mission
    }

})