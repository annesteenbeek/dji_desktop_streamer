import { isClientConnected, getClient } from '../../src/SocketHandler';
import { flightplanToJson, observationsToJson } from '../../lib/geospatial';
import { createAliasedAction } from 'electron-redux';

export const uploadMissionCSV = createAliasedAction(
  'UPLOAD_MISSION_CSV',
  () => {
    let success
    if (isClientConnected()) {
      getClient().uploadMissionCSV()
      success = true
    } else {
      success = false
    }

    return {
      type: 'UPLOAD_MISSION_CSV',
      payload: {
        success
      }
    }
  }
)

export const uploadMissionJson = createAliasedAction(
  'UPLOAD_MISSION_JSON',
  (missionJson) => {
    let success
    if (isClientConnected()) {
      getClient().uploadMissionJson(missionJson)
      success = true
    } else {
      success = false
    }
    return {
      type: 'UPLOAD_MISSION_JSON',
      payload: {
        success
      }
    }
  }
)

export function uploadCurrentMissionThunk() {
  return (dispatch, getState) => {    
    let path = getState().map.flight_path;
    let settings = getState().map.settings;
    let params = getState().map.UAVParams[settings.uavType]
    let name = getState().map.name;
    
    let missionJson = flightplanToJson(path, settings, params, name)
    return dispatch(uploadMissionJson(missionJson))
  }
}

export function uploadALPRMissionThunk() {
  return (dispatch, getState) => {    
    let observation_positions = getState().map.observables.observation_positions;
    let settings = getState().map.settings;
    // let params = getState().map.UAVParams[settings.uavType]
    
    let missionJson = observationsToJson(observation_positions, settings)
    console.log(missionJson)
    return dispatch(uploadMissionJson(missionJson))
  }
}