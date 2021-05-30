import { setFolderDialog, saveStorageLocation, openStorageLocationFolder } from '../../src/Configuration';
import { start_mapping, stop_mapping } from '../../src/ExternalHandler';
import { createAliasedAction } from 'electron-redux';



export const mainSetNetworkInfo = (address, port) => ({
    type: 'SET_NETWORK_INFO',
    payload: {
        address,
        port
    }
})

export const mainSetStorageLocation = createAliasedAction(
    'SET_STORAGE_LOCATION',
    () => {
        let folder;
        try {
            let result = setFolderDialog()
            if (typeof result !== 'undefined'){
                folder = result[0]
                saveStorageLocation(folder)
            }
        } catch(err) {
            console.error(err)
        }
        return {
            type: 'SET_STORAGE_LOCATION',
            payload: {
                storage_location: folder
            }
        }
    }
)

export const mainOpenStorageLocation = createAliasedAction(
    'OPEN_STORAGE_LOCATION',
    () => {
        try {
            openStorageLocationFolder();
        } catch(err) {
            console.log(err)
        }
        return {
            type: 'OPEN_STORAGE_LOCATION'
        }
    }
)

export const mainStartMapping = createAliasedAction(
    'START_MAPPING',
    () => {
        try {
            start_mapping()
        } catch(err) {
            console.log(err)
        }
        return {
            type: 'START_MAPPING'
        }
    }
)

export const mainStopMapping = createAliasedAction(
    'STOP_MAPPING',
    () => {
        try {
            stop_mapping()
        } catch(err) {
            console.log(err)
        }
        return {
            type: 'STOP_MAPPING'
        }
    }
)