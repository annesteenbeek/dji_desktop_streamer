// @flow

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { saveMissionThunk, removeFlightPlan, setFlightSettingThunk, uploadCurrentMissionThunk} from '../../shared/actions';
import { Checkbox, Icon, Button, Grid, Statistic, Divider } from 'semantic-ui-react';
import SettingSlider from '../molecules/SettingSlider';
import SaveNameDialog from '../molecules/SaveNameDialog';
import Typography from '@material-ui/core/Typography';

import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';

class MissionPlannerPanel extends Component<Props> {

    constructor(props) {
        super(props);
        this.state = {
            showSaveDialog: false
        }
    }

    openSaveDialog = () => {
        this.setState({showSaveDialog: true})
    }

    handleSaveClose = (name) => {
        if (name) {
            this.props.saveMission(name)
        }

        this.setState({showSaveDialog: false})
    }

    render() {
        const {
            flightArea,
            nPhotos,
            settings,
            setFlightSetting,
            GSD,
            missionName,
            uploadMission,
            UAVTypes
        } = this.props;

        const flightAreaDisp = Number((flightArea/1000).toFixed(1));

        return (
            <div>
                <Grid.Row>
                    <Typography align='center' variant="h4">
                        { missionName !== '' ? missionName : 'untitled' }
                    </Typography>
                </Grid.Row>
                <Grid.Row>
                    <Grid.Column>
                        <Statistic.Group size='mini'>
                            {/* <Statistic label='Time' value='?'/> */}
                            <Statistic label='km^2' value={flightAreaDisp} />
                            <Statistic label='Images' value={nPhotos} />
                            {/* <Statistic label='Batteries' value='?'/> */}
                        </Statistic.Group>
                    </Grid.Column>
                </Grid.Row>
                <Divider/>
            
                <FormControl style={{paddingBottom: "15px"}}>
                    <InputLabel id="demo-simple-select-label">Model</InputLabel>
                    <Select
                    labelId="demo-simple-select-label"
                    id="demo-simple-select"
                    value={settings.uavType}
                    onChange={(event)=> {
                       setFlightSetting('uavType', event.target.value)
                    }}
                    >
                    {UAVTypes.map((name, index) => {
                        return <MenuItem value={name} key={index}>{name}</MenuItem>
                    })}
                        {/* <MenuItem value={10}>Ten</MenuItem> */}
                    </Select>
                </FormControl>

                <SettingSlider
                    value={settings.altitude}
                    // TODO: insert subtext or something
                    description={`Altitude (m) [GSD: ${GSD} (cm/pixel)]`}
                    min={10}
                    max={50}
                    step={1}
                    callback={(value) => setFlightSetting('altitude', value)}/>
                <SettingSlider
                    value={settings.side_overlay*100}
                    description='Side Overlay (%)'
                    min={30}
                    max={90}
                    step={1}
                    callback={(value) => setFlightSetting('side_overlay', value/100)}/>
                <SettingSlider
                    value={settings.forward_overlay*100}
                    description='Forward Overlay (%)'
                    min={30}
                    max={90}
                    step={1}
                    callback={(value) => setFlightSetting('forward_overlay', value/100)}/>
                <SettingSlider
                    value={settings.speed}
                    description='Flight speed'
                    marks={[{value: 0, label: 'Auto'}]}
                    min={0}
                    max={15}
                    step={1}
                    callback={(value) => setFlightSetting('speed', value)}/>
                {/* <Grid.Row>
                    <Grid.Column width={11} >
                        <p>Obstacle avoidance</p>
                    </Grid.Column>
                    <Grid.Column width={5} >
                        <Checkbox toggle defaultChecked />
                    </Grid.Column>
                </Grid.Row> */}


                <div style={{position:'fixed', bottom: '20px'}}>
                    <Divider />
                    <Grid.Row>
                        <Button color='blue' icon labelPosition='left' onClick={uploadMission}>
                            <Icon name='upload'/> 
                            Upload
                        </Button>
                    </Grid.Row>

                    <Grid.Row>
                        <Button color='blue' icon labelPosition='left' onClick={this.props.removeFlightPlan}>
                            <Icon name='close'/> 
                           Back 
                        </Button>
                        <Button color='blue' icon labelPosition='left' onClick={this.openSaveDialog}>
                            <Icon name='save' />
                            Save
                        </Button>
                    </Grid.Row>
                </div>

                <SaveNameDialog
                    open={this.state.showSaveDialog}
                    onClose={this.handleSaveClose}
                    missionName={this.props.missionName}
                />
            </div>
        )

    }

}

const mapStateToProps = (state) => {
    return {
        showFlightPlan: state.map.showFlightPlan,
        flightArea: state.map.area,
        nPhotos: state.map.nPhotos,
        settings: state.map.settings,
        GSD: state.map.GSD,
        missionName: state.map.name,
        UAVTypes: state.map.UAVTypes
    };
};

const mapDispatchToProps = (dispatch) => ({
    removeFlightPlan: () => dispatch(removeFlightPlan()),
    setFlightSetting: (setting, value) => dispatch(setFlightSettingThunk(setting, value)),
    saveMission: (name) => dispatch(saveMissionThunk(name)),
    uploadMission: () => dispatch(uploadCurrentMissionThunk())
});


export default connect(mapStateToProps, mapDispatchToProps)(MissionPlannerPanel);
