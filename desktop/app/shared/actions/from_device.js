import { setAlert } from './notifications';

// Handles data comming from device

export const setProductState = (product) => ({
    type: 'SET_PRODUCT_STATE',
    payload: {
        product
    }
})

export const setMissionState = (mission) => ({
    type: 'SET_MISSION_STATE',
    payload: {
        mission
    }
})

export const setConnected = (connected) => ({
    type: 'SET_CONNECTED',
    payload: {
        connected
    }
})

export const setLocation = (location) => ({
    type: 'UAV_LOCATION',
    payload: {
        location
    }
})

export function setConnectedThunk(isConnected) {
    return (dispatch, getState) => {
        if(isConnected){
            dispatch(setAlert('success', 'Device Connected'))
        }
        dispatch(setConnected(isConnected))
    }
}

export function setProductStateThunk(product) {
    return (dispatch, getState) => {
        dispatch(setProductState(product))
    }
}

export function setMissionStateThunk(mission) {
    return (dispatch, getState) => {
        dispatch(setMissionState(mission))
    }
}