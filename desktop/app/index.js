// @flow
import React, { Fragment } from 'react';
import { render } from 'react-dom';
import { AppContainer as ReactHotAppContainer  } from 'react-hot-loader';
import Root from './containers/Root';
import './app.global.scss';
import { getInitialStateRenderer } from 'electron-redux';
import configureStore from './shared/store/configureStore';

const store = configureStore(getInitialStateRenderer(), 'renderer');

// render(
//   <AppContainer>
//     <Root store={store}/>
//   </AppContainer>,
//   document.getElementById('root')
// );

// if (module.hot) {
//   module.hot.accept('./containers/Root', () => {
//     const NextRoot = require('./containers/Root'); // eslint-disable-line global-require
//     render(
//       <AppContainer>
//         <NextRoot store={store}/>
//       </AppContainer>,
//       document.getElementById('root')
//     );
//   });
// }

const AppContainer = process.env.PLAIN_HMR ? Fragment : ReactHotAppContainer;

document.addEventListener('DOMContentLoaded', () =>
  render(
    <AppContainer>
      <Root store={store} history={history} />
    </AppContainer>,
    document.getElementById('root')
  )
);