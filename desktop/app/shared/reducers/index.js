// @flow

import { combineReducers } from 'redux';
import devices from './devices';
import configuration from './configuration';
import electron_tasks from './electron_tasks';
import map from './map';
import notifications from './notifications';

export default function getRootReducer(scope = 'main') {
  let reducers = {
    devices,
    configuration,
    electron_tasks,
    map,
    notifications,
  };

  if (scope === 'renderer') {
    reducers = {
      ...reducers,

    };
  }

  return combineReducers({ ...reducers })
}
