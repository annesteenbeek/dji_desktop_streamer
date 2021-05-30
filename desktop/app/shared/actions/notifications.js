
export const setAlert = (severity, message) => ({
    type: 'SET_ALERT',
    payload: {
        severity,
        message
    }
})