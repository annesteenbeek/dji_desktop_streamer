# Disasterprobe 
A complete solution to plan missions and stream video and images from DJI drones to your laptop.
It consists of both a desktop component and an android app. By hosting a hotspot on the android phone, and connecting the desktop app, data is transfered over the local network. 
This is an app I've designed, mostly for research puprposes. Where buildings and roads had to be analyzed in real time during the flight. 

![](https://github.com/annesteenbeek/dji_desktop_streamer/blob/master/screenshots/overview.png?raw=true)
### API keys
You still need to set your own API keys for both the DJI SDK and the google components.
You can search on how to locate these 
In order to get the DJI sdk key, follow this example: [here](https://developer.dji.com/mobile-sdk/documentation/quick-start/index.html)

In order to run the app, you need to add the DJI SDK API key in the `AndroidManifest.xml`

In order to use google maps, you need to get a google maps geo API key as well from the cloud console.
This can be added in the `AndroidManifest.xml` as well, and for the desktop component, in the `desktop/app/conf.js` file.



## Desktop
![](https://github.com/annesteenbeek/dji_desktop_streamer/blob/master/screenshots/desktop_screenshot.png?raw=true)
### Install
To install this package use npm.
The package has been tested using node 10.23.0

```
$ cd desktop
$ npm install
$ cd app 
$ npm install
```


### Running
In order to run/develop the desktop package, both the main (backend) and renderer (frontend) must be running

```
$ cd desktop
$ npm run start-main-dev
# In a new terminal window
$ npm run start-renderer-dev
```

If you need to package the program into an executable (.appimage for linux or .exe for windows)
use:

```
$ cd desktop
$ npm run package-linux
# if on windows
$ npm run package-windows
```

The resulting packages wil be stored in the resources folder

### Video stream
In order to receive a working video stream, we are using an RTMP streaming server on the localhost.
There is a docker image included.
You can either build and run it using vscode, or build it from the command line using the normal docker procedure.

The location of the RTMP stream is:

```
rtmp://localhost:1935/live
```

## Android

![](https://github.com/annesteenbeek/dji_desktop_streamer/blob/master/screenshots/android_screenshot.png?raw=true)
