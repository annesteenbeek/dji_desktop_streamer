// @flow
import React, { Component } from 'react';
const {ipcRenderer} = require('electron');


export class NetworkInfo extends Component {
  constructor(props) {
    super(props);
    this.state = {networkName: "Unknown"};

    var _this = this;
    ipcRenderer.on('NSD:networkInfo', function(event, arg) {
      var address = arg[0] + ":" + arg[1];
        _this.setState({networkName: address});
    });

  }

  
  render() {
    const {
    } = this.props;
    return (
      <div>
          <p>{ this.state.networkName}</p>
      </div>
    );
  }
}