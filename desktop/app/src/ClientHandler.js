// @flow
const { setConnectedThunk, setProductStateThunk, setMissionStateThunk } = require('../shared/actions');
const { csvFileToJson } = require('../src/MissionHandler');
const logger = require('../utils/logger')('ClientHandler');
var configuration = require('./Configuration');
var fs = require('fs');
var path = require('path');
const { dialog } = require('electron');

export default class ClientHandler {
  
  constructor(socket, store) {
    this.socket = socket;
    this.store = store;
    this.setupListeners();
    this.store.dispatch(setConnectedThunk(true));
    this.isActive = true;
    this.imageStream = [];
  }
  
  destroy() {
    logger.info(`Destorying client [${this.socket.id}]`)
    this.socket.disconnect();
    this.isActive = false;
    this.store.dispatch(setConnectedThunk(false));
  }
  
  setupListeners(){
    this.socket.on('probeMission', (data) => {
      this.handleProbeMission(data);
    })
    this.socket.on('productState', (data) => {
      this.handleProductState(data);
    })
    // this.socket.on('photo', (data) => {
    //   this.handlePhoto(data);
    // })
    // this.socket.on('photo', (data, callback) => this.handlePhoto(data, callback));
    this.socket.on('photo_chunk', (data, callback) => this.handlePhotoChunk(data, callback));
    this.socket.on('UAVState', (data) => this.handleUAVState(data));
    this.socket.on('photoEvent', (data) => this.handlePhotoEvent(data));
    // this.socket.on('photo_chunk', (data, callback) => this.handlePhotoChunkDebug(data, callback));
  }

  handlePhotoEvent(data) {
    this.socket.broadcast.emit("photoEvent", data)
    // logger.info("Received photo event: " + data.photoType)
  }

  handleUAVState(data) {
    this.socket.broadcast.emit("UAVState", data)
  }
  
  handlePhoto(data, callback) {
    logger.info('received photo');
    this.store_image(data.filename, data.image_encoded)
    callback(true);
  }

  handlePhotoChunkDebug(data, callback) {
    logger.info('received chunk index: ' + data.chunk_index + '/' +data.total_chunks)
    setTimeout(()=> {
      callback(true);
    }, 500);
  }

  handlePhotoChunk(data, callback) {
    // logger.info('received chunk index: ' + data.chunk_index + "/" +data.total_chunks)
    if (data.chunk_index === 1) {
      this.imageStream = []
      logger.info('Received new image')
      console.time('photochunk')
    }
    this.imageStream.push(data.image_chunk)

    // check if all chunks have been received
    callback(this.imageStream.length === data.chunk_index ? true : false);

    if (data.chunk_index === data.total_chunks) {
      this.store_image(data.filename, this.imageStream.join())
      console.timeEnd('photochunk')
    }
  }

  store_image(filename, image_base64) {
    var storage_location = configuration.getStorageLocation();
    if (!fs.existsSync(storage_location)){
      fs.mkdirSync(storage_location);
    }
    if (filename === '') {
      filename = 'testimg.jpg'
    }
    var location = path.join(storage_location, filename);
    logger.info('Storing ' + location);
    fs.writeFile(location, image_base64, 'base64',(err) => {
      if (err != null) {
        logger.error(err);
        return false;
      }
    })
    return true 
  }
  
  handleProductState(data) {
    this.store.dispatch(setProductStateThunk(data))
  }
  
  handleProbeMission(data) {
    this.store.dispatch(setMissionStateThunk(data))
  }
  
  uploadMissionCSV()  {
    dialog.showOpenDialog({ 
      properties: [ 'openFile' ],
      filters: [{name: 'csv', extensions:['csv', 'CSV']}]
    }, ( filename ) => {
      if (typeof filename !== 'undefined') {
        csvFileToJson(filename[0], (err, data) => {
          if (err){
            logger.error(err);
            // TODO notify about this error
          } else {
            this.socket.emit('missionCSV', data);
          }
        })
      }
    }
    );
  }

  uploadMissionJson(missionJson) {
    this.socket.emit('new_mission', missionJson, (resp)=>{
      // console.log(resp)
    }) 
  }
  
}