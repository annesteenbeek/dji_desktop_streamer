import storage from 'electron-json-storage';
import path from 'path';

function getMissionFolder(root_folder) {
    return path.join(root_folder, 'missions')

}

export function listMissions(dataPath, callback) {
    return storage.keys({dataPath: getMissionFolder(dataPath)}, callback);
}

export function saveMission(dataPath, name, mission, callback) {
   storage.set(name, mission, {dataPath: getMissionFolder(dataPath)}, callback) ;
}

export function loadMission(dataPath, name, callback) {
    storage.get(name, {dataPath: getMissionFolder(dataPath)}, callback)
}

export function removeMission(dataPath, name, callback) {
    storage.remove(name, {dataPath: getMissionFolder(dataPath)}, callback)
}