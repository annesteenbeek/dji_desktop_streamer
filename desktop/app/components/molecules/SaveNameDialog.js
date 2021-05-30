import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import SaveIcon from '@material-ui/icons/Save';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import TextField from '@material-ui/core/TextField';


import DialogActions from '@material-ui/core/DialogActions';
import Dialog from '@material-ui/core/Dialog';

export default function ConfirmationDialogRaw(props) {
  const { onClose, missionName: missionNameProp, open, ...other } = props;
  const [missionName, setMissionName] = React.useState(missionNameProp);
  const [error, setError] = React.useState(false);
  const [help, setHelp] = React.useState('')

  React.useEffect(() => {
    if (!open) {
      setMissionName(missionNameProp);
    }
  }, [missionNameProp, open]);

  const handleCancel = () => {
    onClose();
  };

  const handleOk = () => {
    if (missionName !== ''){
      onClose(missionName);
    } else {
      setError(true);
      setHelp('Please enter valid filename')
    }
  };

  return (
    <Dialog
      disableBackdropClick
      disableEscapeKeyDown
      maxWidth="xs"
      aria-labelledby="confirmation-dialog-title"
      open={open}
      {...other}
    >
      <DialogTitle id="confirmation-dialog-title">Save Mission</DialogTitle>
      <DialogContent dividers>
        <DialogContentText>
          Enter a title
        </DialogContentText>

        <TextField
            error={error}
            helperText={help}
            required
            value={missionName}
            autoFocus
            margin="dense"
            id="name"
            label="Mission name"
            onChange={(event)=>setMissionName(event.target.value)}
          />
     </DialogContent>
      <DialogActions>
        <Button autoFocus onClick={handleCancel} color="primary">
          Cancel
        </Button>
        <Button type='submit' variant='contained' onClick={handleOk} color="primary" startIcon={<SaveIcon />}>
          Ok
        </Button>
      </DialogActions>
    </Dialog>
  );
}

ConfirmationDialogRaw.propTypes = {
  onClose: PropTypes.func.isRequired,
  open: PropTypes.bool.isRequired,
  missionName: PropTypes.string.isRequired,
};