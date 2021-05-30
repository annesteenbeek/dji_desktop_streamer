var winston = require('winston')

var winstonLogger = new (winston.Logger)({
    transports: [
        // new (winston.transports.File) ({
        //     filename: 'MyLogs.txt',
        //     handleExceptions: true,
        //     humanReadableUnhandledException: true,
        //     level: 'info',
        //     timestamp: true,
        //     json: false
        // }),
        new (winston.transports.Console) ({
            level: 'info',
            prettyPrint: true,
            colorize: true,
            timestamp: true
        })
    ]
})

module.exports = function(fileName) {    
    var myLogger = {
        error: function(text) {
            winstonLogger.error(fileName + ': ' + text)
            // console.error(text)
        },
        info: function(text) {
            winstonLogger.info(fileName + ': ' + text)
            // console.log(text);
        }
    }

    return myLogger
}