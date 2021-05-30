// @flow

var csv = require('csvtojson');
var _ = require('lodash');

const expected_header = ['latitude', 'longitude', 'altitude', 'gimbal_pitch', 'yaw'];

// This function returns an async promise that returns the map
var csvFileToJson = function(filename, callback) {
    // callback(err, data)
        csv({
            trim: true,
            noheader: false,
            checkType: true
        })
        .fromFile(filename)
        .on('header', (header)=>{
            if (!_.isEmpty(_.xor(header, expected_header))){
                var headerError = new Error('Missing or incorrect headers');
                callback(headerError, null);
            }
        })
        .on('done',(error)=>{
            if (typeof error !== 'undefined') {
                error = new Error(error);
                callback(error, null);
            }
        })
        .on('end_parsed', (jsonArrObj)=>{
            jsonArrObj['mission_type'] = 'PHOTOPOINTS'
            callback(null, jsonArrObj);
        });
};

module.exports = {
    csvFileToJson: csvFileToJson,
}