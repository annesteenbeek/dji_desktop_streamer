// @flow
import React from 'react';
import { useSelector } from 'react-redux';

import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import MissionBrowser from '../organisms/MissionBrowser';
import MissionPlannerPanel from '../organisms/MissionPlannerPanel';

const useStyles = makeStyles({
  floatingSidebar: {
    backgroundColor: 'white',
    width: '300px',
    padding: '10px',
    float: 'left',
    position: 'fixed',
    zIndex: 100,
    borderRadius: '10px',
    color: 'black',
    left: '15px',
    bottom: '10px',
    top: '10px',
    opacity: 0.95,
    textAlign: 'center',
  },
  floatContainer: {
    height: '100%',
    width: '100%'
  }
})

export default function FloatingSidebar(props) {
  
  const classes = useStyles();

  const showFlightPlan = useSelector(state => state.map.showFlightPlan)

  let header = showFlightPlan ? 'Mission Planner' : 'Mission Browser'

  
  return (
    <div className={classes.floatingSidebar}>
      <Grid className={classes.floatContainer} container wrap='nowrap' justify="flex-start" alignItems="center" direction="column">
        <Grid item >
          <Typography align='center' variant="h4">
            { header }
          </Typography>
          <Divider />
        </Grid>
        {(() => {
          if (showFlightPlan){
            return <MissionPlannerPanel/>
          } else {
            return <MissionBrowser/>
          }
        })()}
      </Grid>  
    </div>
    )
    
  }