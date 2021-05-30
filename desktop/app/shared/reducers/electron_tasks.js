// @flow
import { createReducer } from '@reduxjs/toolkit';

const electronReduce = (state, action) => {
    action.index = state.index + 1;
    return action
}

const electron_tasks = createReducer({index: 0}, {
    UPLOAD_MISSION_FILE: electronReduce,
    SET_FOLDER_DIALOG: electronReduce,
    OPEN_STORAGE_LOCATION: electronReduce,
    SAVE_FLIGHT_PLAN: electronReduce,
    LOAD_FLIGHT_PLAN: electronReduce,
    START_MAPPING: electronReduce,
    STOP_MAPPING: electronReduce,
})

export default electron_tasks;
    