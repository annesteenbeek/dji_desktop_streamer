// @flow
import React, { Component } from 'react';
import { connect } from 'react-redux';
import styles from './InfoBar.scss';
import { Dimmer, Loader, Image, Statistic, Segment, Container, Button, Header, Input, Divider, Icon } from 'semantic-ui-react';
import { uploadALPRMissionThunk, mainSetStorageLocation, mainOpenStorageLocation, mainSetObservations, mainStartMapping, mainStopMapping } from '../../shared/actions/index';

type Props = {
};

class InfoBar extends Component<Props> {
  props: Props;
  
  constructor(props) {
    super(props);
  }
  
  render() {
    const {
      setStorageLocation, openStorageLocation, configuration, devices, startMapping, stopMapping, setObservations, uploadALPRMission 
    } = this.props;
    
    return (
      
      <div className={styles.infobar}>
      
      <Container className={styles.infoContainer}>
      <Header as='h2' textAlign='center'>Info</Header>
      <Segment>
      <Icon name="mobile alternate" color={devices.socketConnected ? 'green' : 'red'} /> <br/>
      <Icon name="plane" color={devices.product.connected ? 'green' : 'red'} /> 
      </Segment>
      
      <Header as='h3'>Image location</Header>
      <Divider/>
      <Segment.Group>
      <Segment>
      <Input
      action={{ color: 'blue', icon: 'folder', onClick: setStorageLocation}}
      style={{paddingLeft: 0}}
      iconPosition='left'
      actionPosition='left'
      placeholder='location'
      value={configuration.storage_location}
      />
      </Segment>
      <Segment>
      <Button primary icon labelPosition='left' onClick={openStorageLocation}>
      Open Folder
      <Icon name='folder open' />
      </Button>
      
      </Segment>
      </Segment.Group>
      
      <Header as='h3'>IP Address</Header>
      <Segment>
        <div style={{color: "black"}} >
          { configuration.network_info.address + ':' + configuration.network_info.port }
        </div>
      </Segment>
      <Divider/>
      <Segment.Group>

      <Segment style={{height: 200}}>
          <Button.Group vertical>
            <Button onClick={startMapping}>Start 3D mapping</Button>
            <Button onClick={stopMapping}>Stop mapping</Button>
            <Button onClick={setObservations}>Process mapping</Button>
            <Button onClick={uploadALPRMission}>Upload observation points</Button>
          </Button.Group>
      {/* <Dimmer active> */}
      {/* <Loader size='mini'>No video connection</Loader> */}
      {/* </Dimmer> */}
      {/* <Image src='' /> */}
      </Segment>
      </Segment.Group>
      </Container>
      </div>
      );
    }
  }
  
  const mapStateToProps = (state) => {
    return {
      configuration: state.configuration,
      devices: state.devices
    };
  };
  
  const mapDispatchToProps = (dispatch) => ({
    setStorageLocation: () => {dispatch(mainSetStorageLocation())},
    openStorageLocation: () => {dispatch(mainOpenStorageLocation())},
    startMapping: () => {dispatch(mainStartMapping())},
    stopMapping: () => {dispatch(mainStopMapping())},
    setObservations: () => {dispatch(mainSetObservations())},
    uploadALPRMission: () => {dispatch(uploadALPRMissionThunk())}
  });
  
  export default connect(mapStateToProps, mapDispatchToProps)(InfoBar);
  