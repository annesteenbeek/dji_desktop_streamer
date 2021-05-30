const logger = require('../utils/logger')('ImageHandler');
const express = require('express')
const multer = require('multer')
const FormData = require('form-data');
// const symlinkDir = require('symlink-dir');
const os = require('os');
const fs = require('fs');
const path = require('path');
const shell = require('electron').shell;

var configuration = require('./Configuration');

const storage = multer.diskStorage({
  destination: os.tmpdir(),
  filename: function (req, file, cb) {
    console.time('photopost')
    cb(null, file.originalname);
  }

})

const upload = multer({storage: storage})


export default class ImageHandler {
  constructor(store) {
    this.store = store;
    this.app = express();

    this.image_shown = false;

    this.app.post('/mission_image', upload.single('image'), (req, res) => {
      logger.info("received image")
      console.timeEnd('photopost')

      let image_folder = path.join(configuration.getStorageLocation(), "images");
      let mission_folder = path.join(image_folder, req.body.missionName);
      let alpr_folder = path.join(mission_folder, 'processed')
      let storage_path = path.join(mission_folder, req.file.filename);
      let alpr_storage_path = path.join(alpr_folder, req.file.filename)

      if (!fs.existsSync(mission_folder)) {
        fs.mkdirSync(mission_folder)
      }
      if (!fs.existsSync(alpr_folder)) {
        fs.mkdirSync(alpr_folder)
      }

      fs.renameSync(req.file.path, storage_path)
      logger.info('Moving file to: ' + storage_path)

      // symlinkDir(mission_folder, path.join(image_folder, "latest"))

      if (true) {
      // if (req.body.missionName == "ALPR") {
        // forward to  ALPR
        let return_image = true

        var form = new FormData();
        form.append('return_image', return_image.toString());
        form.append('image', fs.createReadStream(storage_path))

        logger.info('Sending image to ALPR');
        form.submit('http://localhost:6969/detect_licenses', function(err, res) {
          if(err) {
            logger.error("Received error from ALPR: " + err)
          } else {
            res.resume()

            var body = '';
            res.on('data', function(data){
              body += data.toString();
            });
          
            res.on('end', function(){
              if (res.statusCode == 200) { // everything ok
                let res_json = JSON.parse(body)

                // console.log(res_json['results'])

                if(res_json['image'] !== null) {
                  logger.info("Response image stored")
                  let image_buffer = new Buffer(res_json['image'], 'base64')
                  // fs.writeFile('/tmp/FINISHEDIMG.jpg', image_buffer, (err) => {
                  fs.writeFile(alpr_storage_path, image_buffer, (err) => {
                    if (err) {
                      logger.error("Unable to write image: " + err)
                    } else {
                      if (! this.image_shown) {
                        // this.image_shown = shell.openItem("/tmp/FINISHEDIMG.jpg");
                        this.image_shown = shell.openItem(alpr_storage_path);
                      }
                    }
                  })
                }
              } else {
                // console.log(res.statusCode)
                logger.error("Received status ALPR status code: " + res.statusCode)
              }
            });
          }
        })
      }

      return res.send({success: true})
    })

    let port = 3005;
    this.app.listen(port, () => {
      logger.info(`Listening to requests on http://localhost:${port}`);
    });

  }


}
