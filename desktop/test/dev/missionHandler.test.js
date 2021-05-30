// @flow
import { csvFileToJson } from '../../app/src/MissionHandler';

const csvFile = __dirname + '/data/testCoordinates.csv';
const csvFile_noHeader = __dirname + '/data/testCoordinates.noHeader.csv';

describe('Check return values', () => {
    it('Should return correct map values', done => {
        expect.assertions(1);
        const expected_result = {"alt": 26,
                         "cam_pitch": -33,
                         "cam_yaw": -5,
                         "lat": 45.70500779,
                         "lon": 4.84985288};

        const expected_last_result = {
                        "alt": 41,
                        "cam_pitch": -27,
                        "cam_yaw": -5,
                        "lat": 45.70503451,
                        "lon": 4.85016863};

        csvFileToJson(csvFile, function(err, result){
            // expect(result[0]).toEqual(expected_result);
            expect(result[result.length -1]).toEqual(expected_last_result);
            done();
        });
    });

});

describe('Check reject', () => {
    it('Should throw error when receiving file without header', done => {
        expect.assertions(1);
        csvFileToJson(csvFile_noHeader, function(err, data){
            // console.log(err)
            // expect(err).toEqual(new Error("Missing or incorrect headers"));
            expect(data).toEqual(null);
            done();
        });
    });
})
