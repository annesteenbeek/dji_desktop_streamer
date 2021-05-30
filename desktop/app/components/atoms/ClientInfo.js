// @flow
import React, { Component } from 'react';
const {ipcRenderer} = require('electron');


export class ClientInfo extends Component {
    constructor(props) {
        super(props);
        this.state = {clientID: "Unknown"};

        var _this = this;
        ipcRenderer.on('client:start', function(event, arg) {
            _this.setState({clientID: arg});
        });
    }

    render() {
        const {
        } = this.props;
        return (
        <div>
            <p>Client ID: { this.state.clientID }</p>
        </div>
        );
    }
}