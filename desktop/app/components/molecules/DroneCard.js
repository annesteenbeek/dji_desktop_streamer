// @flow
import React, { Component } from 'react';
import {Dimmer, Container, Card, Image, Button, Header } from 'semantic-ui-react';

type Props = {
    clientID: string,
    dimmed: boolean
};

export default class DroneCard extends Component<Props> {
  props: Props;

    constructor(props) {
        super(props);
        this.uploadMissionFile = this.props.uploadMissionFile;
        this.clientID = this.props.clientID;
    }

    openDialog = () => {
        this.uploadMissionFile(this.clientID);
    }

  render() {
    const {
        clientID, dimmed
    } = this.props;
    return (
        <Dimmer.Dimmable dimmed={dimmed} as={Card}>
            <Dimmer active={dimmed}>
            <Header as='h2' inverted>
                Disconnected
            </Header>
            </Dimmer>
            <Image src="./media/mavic.png" size="large"/>
            <Card.Content>
            <Card.Header>
                Mavic
            </Card.Header>
            <Card.Description>
                { clientID }
            </Card.Description>
            </Card.Content>
            <Card.Content>
                <Button primary compact onClick={this.openDialog}>
                    Upload Mission
                </Button>
            </Card.Content>
        </Dimmer.Dimmable>
    );
  }
}