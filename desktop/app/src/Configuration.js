// @flow
var { dialog, shell } = require('electron');
const settings = require('electron-settings');
const os = require('os');
const fs = require('fs');
const util = require('util');
const path = require('path');

import { initialState as initialConf } from '../shared/reducers/configuration';
import { initialState as initialMap} from '../shared/reducers/map';

import defaultParams from '../lib/UAVParams_default';

export function getConfiguration() {
  let initial = {...initialConf,
      storage_location: getStorageLocation(),
  }
  return initial
}

export function getUAVParams() {
  let filename = path.join(getStorageLocation(), "UAVParams.json")
  // check if file exists
  if (!fs.existsSync(filename)){
    fs.writeFileSync(filename, JSON.stringify(defaultParams, null, 2), 'utf-8');
  }
  let params_raw = fs.readFileSync(filename);
  let params = JSON.parse(params_raw)

  // TODO: VALIDATE

  let initial = {...initialMap,
      UAVParams: params,
      UAVTypes: Object.keys(params)
  }
  
  return initial
}

export function saveStorageLocation(location) {
  settings.set('storage_location', location)
}

export function openStorageLocationFolder() {
  let location = getStorageLocation() + '/'
  shell.openItem(location)
}

export function getStorageLocation() {
  let storage_location = settings.get('storage_location', path.join(os.homedir(), 'disasterprobe'))
  if (!fs.existsSync(storage_location)) {
    fs.mkdirSync(storage_location);
    fs.mkdirSync(path.join(storage_location, "missions"))
    fs.mkdirSync(path.join(storage_location, "images"))
  }
  return storage_location
}

export function setFolderDialog() {
  return dialog.showOpenDialog({
          properties: ['openDirectory', 'createDirectory', 'promptToCreate'],
      })
}
