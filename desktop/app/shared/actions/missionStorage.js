import { listMissions, saveMission, loadMission, removeMission } from '../../lib/storage';
import { setMissionName, setMission } from './map';
import { setAlert } from './notifications';

export const setMissionList = (missions) => ({
  type: 'SET_MISSION_LIST',
  payload: {
    missions
  },
  meta: {
    scope: 'local'
  }
})

// THUNKS
export function populateMissionListThunk() {
  return (dispatch, getState) => {
    let mission_path = getState().configuration.storage_location;
    if (mission_path === '') { return }
    listMissions(mission_path, (error, keys)=> {
      return dispatch(setMissionList(keys))
    });
  }
}

export function saveMissionThunk(name) {
  return (dispatch, getState) => {
    dispatch(setMissionName(name))
    let mission = getState().map;
    let root_folder = getState().configuration.storage_location
    return saveMission(root_folder, name, mission, (error)=> {
      if (error) {
        return dispatch(setAlert('error', error))
      } else {
        dispatch(setAlert('success', 'Saved mission: ' + name))
        return dispatch(populateMissionListThunk())
      }
    });
  }
}

export function loadMissionThunk(name) {
  return (dispatch, getState) => {
    let root_folder = getState().configuration.storage_location
    return loadMission(root_folder, name, (error, data) => {
      if (error) {
        return dispatch(setAlert('error', error))
      } else {
        dispatch(setAlert('success', 'Loaded mission: '+ name))
        return dispatch(setMission(data))
      }
    })
  }
}

export function removeMissionThunk(name) {
  return (dispatch, getState) => {
    let root_folder = getState().configuration.storage_location
    return removeMission(root_folder, name, (error)=> {
      if (error) {
        return dispatch(setAlert('error', error))
      } else {
        dispatch(setAlert('success', 'Removed mission: '+ name))
        return dispatch(populateMissionListThunk())
      }
    })
  }
}