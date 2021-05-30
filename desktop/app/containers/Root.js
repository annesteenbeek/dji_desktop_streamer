// @flow
import React, { Component } from 'react';
import { Provider } from 'react-redux';
import { ConnectedRouter } from 'react-router-redux';
import { hot } from 'react-hot-loader/root';
import { addDevice, setDeviceState } from '../shared/actions/index';
import { ipcRenderer } from 'electron';
import { Grid, Divider } from 'semantic-ui-react';
import Container from 'semantic-ui-react/dist/commonjs/elements/Container/Container';
import styles from './Root.scss';
import InfoBar from '../components/organisms/InfoBar';
import MapBuilder from '../components/organisms/MapBuilder';
import FloatingSidebar from '../components/atoms/FloatingSidebar';
import Notifications from '../components/molecules/Notifications';

type Props = {
  store: Store;
  // history: History;
};


const Root = ({ store }: Props) => (
  <Provider store={store}>
      <Grid className={styles.mainGrid}>
        <Grid.Row className="noPadding">
          <Grid.Column className="noPadding" width={13}>
            <FloatingSidebar/>
            <MapBuilder/>
          </Grid.Column>
          <Grid.Column className="noPadding touchBottom" width={3}>
            <InfoBar/>
          </Grid.Column>
        </Grid.Row>
      </Grid>
      <Notifications/>
  </Provider>
);

export default hot(Root);
