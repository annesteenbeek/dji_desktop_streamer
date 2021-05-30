// @flow
import { createStore, applyMiddleware, compose } from 'redux';
import { persistState } from 'redux-devtools';
import { createLogger } from 'redux-logger';
import thunk from 'redux-thunk';
import promise from 'redux-promise';
import {
  forwardToMain,
  forwardToMainWithParams,
  forwardToRenderer,
  triggerAlias,
  replayActionMain,
  replayActionRenderer,
} from 'electron-redux';
import getRootReducer from '../reducers';
import * as actions from '../actions';

export default function configureStore(initialState, scope='main') {
  const logger = createLogger({
    level: scope === 'main' ? undefined : 'info',
    collapsed: true,
  })

  let middleware = [
    thunk,
    promise,
  ]

  if (!process.env.NODE_ENV) {
    // middleware.push(logger);
  }

  if (scope === 'renderer') {
    middleware = [
      forwardToMain,
      ...middleware,
      logger,
    ];
  }

  if (scope === 'main') {
    middleware = [
      triggerAlias,
      ...middleware,
      forwardToRenderer,
    ];
  }

  const enhanced = [
    applyMiddleware(...middleware),
  ]

  let composeEnhancer = compose;
  if (/*! process.env.NODE_ENV && */scope === 'renderer') {
    if (window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__) {
      composeEnhancer = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__({
        actions
      })
    }
    enhanced.push(persistState(
      window.location.href.match(
        /[?&]debug_session=([^&]+)\b/
      )
    ));
  } 

  const rootReducer = getRootReducer(scope);
  const enhancer = composeEnhancer(...enhanced);
  const store = createStore(rootReducer, initialState, enhancer);
  // const store = createStore(rootReducer, enhancer);

  if (!process.env.NODE_ENV && module.hot) {
    module.hot.accept('../reducers', () => {
      store.replaceReducer(require('../reducers'));
    });
  }

  if (scope === 'main') {
    replayActionMain(store);
  } else {
    replayActionRenderer(store);
  }

  return store;
}

// let store;

// module.exports = function(scope) {
//   if (typeof store === 'undefined') {
//     if (scope === 'main'){
//       store = configureStore(global.state, scope);
//     } else {
//       store = configureStore(getInitialStateRenderer(), 'renderer');
//     }
//   }  
//   return store
// }