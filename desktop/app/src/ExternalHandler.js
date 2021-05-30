// @flow
const logger = require('../utils/logger')('StateMachine');
const os = require('os');
const fs = require('fs');
// const readline = require('readline');
const { execSync, spawn } = require("child_process");

// var utm = new utmObj();

// Starts the Ros node 
let child 
let child_exited = true 
export function start_mapping() {
  if (child_exited) {
    logger.info("Starting mapping process")
    child_exited = false;
    let cmd = "roslaunch realm_ros disasterprobe_reco_ros.launch"
    child = spawn(cmd, {
      detached: true,
      stdio: 'ignore',
      shell: true
    });

    child.on('exit', (code, signal) =>{
      logger.info(`child exited with code ${code} and signal ${signal}`)
      child_exited = true;
    })

    child.on('close', (code) => {
      logger.info(`child process close all stdio with code ${code}`);
    });

    child.on('error', (err) => {
      logger.error('Failed to start subprocess.');
    });
    // child.unref()
  } else {
    logger.warning("Child already running")
  }
}

// Shuts the Ros node down
export function stop_mapping() {
  if (typeof(child) != 'undefined') {
    logger.info("Sending kill to child process");
    process.kill(-child.pid)
    // child.kill('SIGTERM');
  }
}

export function determine_observations() {
  // LATEST_PLY_DIR="$(ls -td realm_ros/output/*/mosaicing/elevation/ply | head -1)" 
  // rosrun disasterprobe_bridge plan_observations "$LATEST_PLY_DIR/elevation.ply" "$VEHICLES" out.txt
  let cmd = "/home/itc/catkin_workspaces/openrealm_ws/src/OpenREALM_ROS1_Bridge/run.sh";
  //TODO: make this async with promise
  execSync(cmd, (error, stdout, stderr) => {
      if (error) {
          console.log(`error: ${error.message}`);
          return;
      }
      if (stderr) {
          console.log(`stderr: ${stderr}`);
          return;
      }
      console.log(`stdout: ${stdout}`);
  })


  // TODO: fix UTM to use WSG (current zone is static)
  let out_path='/home/itc/catkin_workspaces/openrealm_ws/src/OpenREALM_ROS1_Bridge/out.txt'
  let vehicle_path='/home/itc/catkin_workspaces/openrealm_ws/src/OpenREALM_ROS1_Bridge/vehicles.txt'

  var out_lines = fs.readFileSync(out_path, 'utf-8')
    .split('\n');
  var vehicle_lines = fs.readFileSync(vehicle_path, 'utf-8')
    .split('\n');
  vehicle_lines.pop() // remove last empty entry
  out_lines.pop()

  let observation_positions = [];
  out_lines.forEach((line => {
    let line_split = line.split(' ');

    let [lat, lng, alt, camera_pitch, yaw] = line_split.map(Number)

    let observation = {
      lat,
      lng,
      alt,
      yaw,
      camera_pitch,
    }
    observation_positions.push(observation);
  }))


  let vehicle_positions = []
  vehicle_lines.forEach((line => {
    let line_split = line.split(' ');

    let [lat, lng, h, w, theta, alt] = line_split.map(Number)

    let vehicle = {
      lat,
      lng,
      alt,
      theta,
    }
    vehicle_positions.push(vehicle);
  }))
  return [vehicle_positions, observation_positions]
}



