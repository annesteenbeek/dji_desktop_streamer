// @flow

import React, { Component } from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Slider from '@material-ui/core/Slider';
import Input from '@material-ui/core/Input';


class SettingSlider extends Component<Props> {
  
  
  constructor(props) {
    super(props);
    this.state = {
      value: this.props.value
    };


  }
  
  handleSliderChange = (e, newValue) => {
    this.setState({value: newValue});
  }
  
  handleInputChange = (event) => {
    let newValue = event.target.value === '' ? '' : Number(event.target.value);
    this.setState({value: newValue});
    this.props.callback(newValue)
  };
  
  render() {
    
    const {
      description,
      min,
      max,
      step,
      callback,
      marks
    } = this.props;
    
    let value = this.state.value;
    
    return (
    <div style={{width: '100%'}}>
      <Typography align='left' gutterBottom>
        { description }
      </Typography>
      <Grid container spacing={2} alignItems="center">
        <Grid item xs>
          <Slider
            max={max}
            min={min}
            step={step}
            marks={marks==='undefined' ? false : marks}
            value={typeof value === 'number' ? value : 0}
            aria-labelledby="input-slider"
            onChangeCommitted={(e, value) => callback(value)}
            onChange={this.handleSliderChange}
          />
        </Grid>
        <Grid item>
          <Input
            value={value}
            margin="dense"
            onChange={this.handleInputChange}
            inputProps={{
              step: step,
              min: min,
              max: max,
              type: 'number',
              'aria-labelledby': 'input-slider',
            }}
          />
        </Grid>
      </Grid>
    </div>
    )
    }
  }
  
  export default SettingSlider;
  