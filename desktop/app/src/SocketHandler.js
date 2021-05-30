// @flow
const Server = require('socket.io');
const ClientHandler = require('../src/ClientHandler');
const logger = require('../utils/logger')('SocketHandler');

let client;

export function hasClient() {
  return typeof client !== 'undefined'
}

export function isClientConnected() {
  if (hasClient()) {
    return true
  }
  return false
}

export function getClient() {
  return client;
}

export default class SocketHandler {
  constructor(store) {
    this.isRunning = false;
    this.store = store;
    this.count = 0
    this.io
  }
  
  start(port) {

    logger.info('starting socket.io server')
    this.io = new Server();
    this.io.listen(port)
    
    this.io.on('connection', (socket) => {
      logger.info(`Client [${socket.id}] connected`)
      
      if (socket.handshake.headers.clienttype === 'ROS') {
        logger.info('ROS client connected')
        return
      }

      if (hasClient()) {
        logger.error('Multiple connections, overwriting initial connection')
        this.endClient()
      }
      client = new ClientHandler(socket, this.store);
      socket.on('disconnect', (reason)=> {
        logger.info(`Client [${socket.id}] disconnected: ${reason}`)
        this.endClient();
      })
    })
    this.isRunning = true
  }
  
  stop() {
    logger.info('Closing socket.io server')
    this.endClient()

    this.io.close()
    this.isRunning = false
  }

  endClient() {
    if (hasClient()) {
      client.destroy()
      client = undefined
    }
  }
}
