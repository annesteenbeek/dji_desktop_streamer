// @flow
const EventEmitter = require('events');
const ip = require('ip');
const bonjour = require('bonjour');
const getPort = require('get-port');
const logger = require('../utils/logger')('NetworkServices');
const { PORT, SERVICENAME } = require('../conf');

import { mainSetNetworkInfo } from '../shared/actions';

const checkFrequency = 1000;

export default class NetworkServices extends EventEmitter {
  
  constructor(store) {
    super()
    this.store = store
    this.address = ip.address()
    this.port
    this.service
    this.bonjour
  }

  setup() {
    this.startService()

    this.interval = setInterval(() => {
      let new_address = ip.address();
      if (this.address !== new_address) {
        this.address = new_address
        logger.info('IP changed: ' + this.address);
        if (ip.isPrivate(this.address)) {
          logger.info('IP is local')
        }

        this.stopService(()=>{
          this.startService()
          this.start_timeout = false
        })
      }
    }, checkFrequency)
  }

  setupDiscovery(callback) {
    // Find an open port, with default port as preference
    getPort({port: PORT}).then(open_port => {
      logger.info(`Starting bonjour on [${this.address}:${open_port}]`)
      this.port = open_port

      this.bonjour = bonjour()
      this.service = this.bonjour.publish({
        host: this.address,
        name: SERVICENAME,
        type: 'http',
        port: open_port 
      });
      callback(true)
      return
    }).catch(err => {
      logger.error(err)
      logger.error(err.stack)
      callback(false)
    })
  }

  startService() {
    this.setupDiscovery((success)=> {
      if(success) {
        logger.info('Started bonjour service')
        this.store.dispatch(mainSetNetworkInfo(this.address, this.port))
        this.emit('start', this.port)
      } else {
        logger.error('Unable to start, stopping network services')
        this.destroy();
      }
    })
  }

  stopService(callback) {
    this.service.stop(()=> {
      this.bonjour.destroy();
      logger.info('Stopped bonjour service')
      this.emit('stopped');
      if (callback) callback()
    })
  }

  destroy() {
    clearInterval(this.interval);
    this.stopService()
  }
}

