// @flow

import React from 'react';
import { connect } from 'react-redux';
import DroneCardList from '../components/organisms/DroneCardList';
import { Container, Divider } from 'semantic-ui-react';
import { uploadMissionFile } from '../shared/actions/index';

const mapStateToProps = (state) => ({
    devices: state.devices
})

const mapDispatchToProps = (dispatch) => ({
    uploadMissionFile: (deviceID) => {dispatch(uploadMissionFile(deviceID))}
})

const DroneContainer = connect(
    mapStateToProps, mapDispatchToProps
)(DroneCardList);

export default DroneContainer;