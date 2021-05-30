import { createReducer } from '@reduxjs/toolkit';

const initialState = {
    alert: {
        severity: '',
        message: '',
        index: 0
    }
}

const notifications = createReducer(initialState, {
    SET_ALERT: (state, action) => {
        state.alert = {...action.payload, index: state.alert.index + 1};
    }
})

export default notifications