// @flow

import React, { Component } from 'react';
import DroneCard from '../molecules/DroneCard';
import { Grid, Container, Divider } from 'semantic-ui-react';

export default class DroneCardList extends Component<Props> {
    constructor(props) {
        super(props);
    }

    render() {
        const {
            devices, uploadMissionFile
        } = this.props;
        return(
            <Container>
                <Divider/>
                <Grid columns={2} divided>
                {devices.map(device =>
                    <Grid.Column key={device.id}>
                    <DroneCard
                        key={device.id}
                        clientID={device.id}
                        dimmed={!device.active}
                        uploadMissionFile={uploadMissionFile}
                        />
                    </Grid.Column>
                )}
                </Grid>
            </Container>    
            )
    }
}
