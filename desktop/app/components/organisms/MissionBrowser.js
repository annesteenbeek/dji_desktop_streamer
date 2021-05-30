// @flow

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { insertFlightPlanThunk,
          uploadMissionCSV,
          removeMissionThunk,
          setFlightSettingThunk,
          populateMissionListThunk,
          loadMissionThunk, 
          saveMissionThunk} from '../../shared/actions/index';
import RemoveConfirmDialog from '../molecules/RemoveConfirmDialog';

import { withStyles } from '@material-ui/core/styles';
import List from '@material-ui/core/List';
import Button from '@material-ui/core/Button';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import ListItemText from '@material-ui/core/ListItemText';
import Avatar from '@material-ui/core/Avatar';
import IconButton from '@material-ui/core/IconButton';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import MapIcon from '@material-ui/icons/Map';
import DeleteIcon from '@material-ui/icons/Delete';

const styles = theme => ({
  missionList: {
    width: '100%',
    // maxWidth: 360,
    backgroundColor: theme.palette.background.paper,
    position: 'relative',
    overflow: 'auto',
    maxHeight: 500,
    // alignSelf: 'stretch'
  },
  listSection: {
    backgroundColor: 'inherit',
  },
  ul: {
    backgroundColor: 'inherit',
    padding: 0,
  },
  deleteButton: {
    '&:hover': {
      color: 'red'
    }
  },
  mainContainer: {
    height: '100%',
    width: '100%'
  }
});

class MissionBrowser extends Component<Props> {

  constructor(props) {
    super(props)
    this.state = {
      openConfirm: false,
      removeName: '',
    }
  }

  componentDidMount() {
    this.props.populateMissionList();
  }

  handleClose = (result) => {
    if (result) {
      this.props.removeMission(this.state.removeName)
    }

    this.setState({removeName: '', openConfirm: false})
  }

  openConfirmRemoveDialog = (name) => {
    this.setState({removeName: name, openConfirm: true})
  }
  
  render() {

    const {
      classes,
      insertFlightPlan,
      uploadMissionCSV,
      mission_list,
      loadMission,
      devices
    } = this.props;
    
    return (
      <Grid className={classes.mainContainer} container wrap='nowrap' justify="flex-start" alignItems="stretch" direction="column">
        <Grid item >
          <Typography variant="h6" className={classes.title}>
            Stored Missions
          </Typography>
            <List className={classes.missionList} dense={false}>
              {mission_list.map((name, index)=> {
                return (
                <ListItem 
                  button 
                  onClick={() => loadMission(mission_list[index])}
                  key={index}
                >
                  <ListItemAvatar>
                    <Avatar>
                      <MapIcon/>
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={name}
                    // secondary={secondary ? 'Secondary text' : null}
                  />
                  <ListItemSecondaryAction>
                    <IconButton onClick={()=>this.openConfirmRemoveDialog(mission_list[index])}  className={classes.deleteButton} edge="end" aria-label="delete">
                      <DeleteIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
                )
              })}
            </List>
        </Grid>

      <Grid container style={{marginTop: 'auto'}} spacing={2} justify="center">
        <Grid item>
          <Button variant="contained" color="primary" onClick={insertFlightPlan}>
            New Mission
          </Button>
        </Grid>
        <Grid item>
          <Button variant="contained" color="primary" onClick={uploadMissionCSV} disabled={!devices.product.connected}>
            CSV
          </Button>
        </Grid>
      </Grid>

      <RemoveConfirmDialog
        keepMounted
        missionName={this.state.removeName}
        open={this.state.openConfirm}
        onClose={this.handleClose}
        />

      </Grid>
      )
      
    }
    
  }
  
  const mapStateToProps = (state) => {
    return {
      showFlightPlan: state.map.showFlightPlan,
      mission_list: state.configuration.mission_list,
      devices: state.devices
    };
  };
  
  const mapDispatchToProps = (dispatch) => ({
    uploadMissionCSV: () => dispatch(uploadMissionCSV()),
    insertFlightPlan: () => dispatch(insertFlightPlanThunk()),
    populateMissionList: () => dispatch(populateMissionListThunk()),
    loadMission: (name) => dispatch(loadMissionThunk(name)),
    removeMission: (name) => dispatch(removeMissionThunk(name)),
  });
  
  
  export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(MissionBrowser));
  