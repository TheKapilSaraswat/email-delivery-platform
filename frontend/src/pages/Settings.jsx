import { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Paper, TextField, Button, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, IconButton, Divider, Dialog, DialogTitle,
  DialogContent, DialogActions, CircularProgress, Snackbar,
} from '@mui/material';
import { Delete, Add, ContentCopy, Visibility, VisibilityOff } from '@mui/icons-material';
import api from '../services/api';

export default function Settings() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const [profile, setProfile] = useState({ name: user.name || '', email: user.email || '', password: '' });
  const [apiKeys, setApiKeys] = useState([]);
  const [newKeyName, setNewKeyName] = useState('');
  const [message, setMessage] = useState({ text: '', type: 'info' });
  const [showKey, setShowKey] = useState('');
  const [revokeId, setRevokeId] = useState(null);
  const [loading, setLoading] = useState({ profile: false, keys: false, create: false, revoke: false });
  const [showApiKeys, setShowApiKeys] = useState({});

  const fetchKeys = useCallback(() => {
    setLoading(prev => ({ ...prev, keys: true }));
    api.get('/api-keys')
      .then(({ data }) => setApiKeys(data))
      .catch(() => setMessage({ text: 'Failed to load API keys', type: 'error' }))
      .finally(() => setLoading(prev => ({ ...prev, keys: false })));
  }, []);

  useEffect(() => { fetchKeys(); }, [fetchKeys]);

  const handleUpdateProfile = async () => {
    if (!profile.name.trim()) {
      setMessage({ text: 'Name is required', type: 'error' });
      return;
    }
    if (!profile.email.trim()) {
      setMessage({ text: 'Email is required', type: 'error' });
      return;
    }
    setLoading(prev => ({ ...prev, profile: true }));
    try {
      const payload = { name: profile.name.trim(), email: profile.email.trim() };
      if (profile.password) payload.password = profile.password;
      const { data } = await api.put('/auth/profile', payload);
      localStorage.setItem('user', JSON.stringify({ id: data.id, email: data.email, name: data.name }));
      setMessage({ text: 'Profile updated successfully', type: 'success' });
      setProfile({ ...profile, password: '' });
    } catch (err) {
      setMessage({ text: err.response?.data?.error || 'Error updating profile', type: 'error' });
    } finally {
      setLoading(prev => ({ ...prev, profile: false }));
    }
  };

  const handleCreateKey = async () => {
    if (!newKeyName.trim()) return;
    setLoading(prev => ({ ...prev, create: true }));
    try {
      const { data } = await api.post('/api-keys', { name: newKeyName.trim() });
      setShowKey(data.keyValue);
      setNewKeyName('');
      fetchKeys();
    } catch (err) {
      setMessage({ text: err.response?.data?.error || 'Failed to create API key', type: 'error' });
    } finally {
      setLoading(prev => ({ ...prev, create: false }));
    }
  };

  const handleRevokeKey = async (id) => {
    setLoading(prev => ({ ...prev, revoke: true }));
    try {
      await api.delete(`/api-keys/${id}`);
      setRevokeId(null);
      fetchKeys();
      setMessage({ text: 'API key revoked', type: 'success' });
    } catch (err) {
      setMessage({ text: err.response?.data?.error || 'Failed to revoke key', type: 'error' });
    } finally {
      setLoading(prev => ({ ...prev, revoke: false }));
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text).then(() => {
      setMessage({ text: 'Copied to clipboard', type: 'success' });
    });
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={700}>Settings</Typography>

      <Snackbar open={!!message.text} autoHideDuration={4000} onClose={() => setMessage({ text: '', type: 'info' })}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}>
        <Alert severity={message.type} onClose={() => setMessage({ text: '', type: 'info' })} sx={{ width: '100%' }}>
          {message.text}
        </Alert>
      </Snackbar>

      {showKey && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setShowKey('')}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography sx={{ flexGrow: 1, fontFamily: 'monospace', wordBreak: 'break-all' }}>
              <strong>API Key:</strong> {showKey}
            </Typography>
            <IconButton size="small" onClick={() => copyToClipboard(showKey)} title="Copy to clipboard">
              <ContentCopy fontSize="small" />
            </IconButton>
          </Box>
          <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
            Copy this key now. It will not be shown again.
          </Typography>
        </Alert>
      )}

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Profile</Typography>
        <TextField label="Name" fullWidth sx={{ mb: 2 }} value={profile.name}
          onChange={(e) => setProfile({ ...profile, name: e.target.value })} />
        <TextField label="Email" type="email" fullWidth sx={{ mb: 2 }} value={profile.email}
          onChange={(e) => setProfile({ ...profile, email: e.target.value })} />
        <TextField label="New Password" type="password" fullWidth sx={{ mb: 2 }} value={profile.password}
          onChange={(e) => setProfile({ ...profile, password: e.target.value })}
          placeholder="Leave blank to keep current password"
          helperText="Minimum 6 characters" />
        <Button variant="contained" onClick={handleUpdateProfile} disabled={loading.profile}
          startIcon={loading.profile ? <CircularProgress size={16} color="inherit" /> : null}>
          {loading.profile ? 'Saving...' : 'Update Profile'}
        </Button>
      </Paper>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>API Keys</Typography>
        <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
          API keys allow you to send emails programmatically. Use the key in the <code>x-api-key</code> header.
        </Typography>

        <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
          <TextField label="Key Name" size="small" value={newKeyName}
            onChange={(e) => setNewKeyName(e.target.value)}
            placeholder="e.g., production-server" />
          <Button variant="contained" startIcon={loading.create ? <CircularProgress size={16} color="inherit" /> : <Add />}
            onClick={handleCreateKey} disabled={loading.create || !newKeyName.trim()}>
            {loading.create ? 'Creating...' : 'Create Key'}
          </Button>
        </Box>

        {loading.keys ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress /></Box>
        ) : (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Key</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {apiKeys.map((k) => (
                  <TableRow key={k.id}>
                    <TableCell>{k.name}</TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                          {showApiKeys[k.id] ? k.keyValue : k.keyValue?.substring(0, 12) + '...'}
                        </Typography>
                        <IconButton size="small" onClick={() => setShowApiKeys(prev => ({ ...prev, [k.id]: !prev[k.id] }))}>
                          {showApiKeys[k.id] ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                        </IconButton>
                        <IconButton size="small" onClick={() => copyToClipboard(k.keyValue)}>
                          <ContentCopy fontSize="small" />
                        </IconButton>
                      </Box>
                    </TableCell>
                    <TableCell>{k.created_at}</TableCell>
                    <TableCell>
                      <Alert severity={k.active !== false ? 'success' : 'error'} sx={{ py: 0, px: 1 }}>
                        {k.active !== false ? 'Active' : 'Revoked'}
                      </Alert>
                    </TableCell>
                    <TableCell align="right">
                      {k.active !== false && (
                        <IconButton color="error" onClick={() => setRevokeId(k.id)} size="small"><Delete /></IconButton>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
                {apiKeys.length === 0 && (
                  <TableRow><TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                    <Typography color="textSecondary">No API keys yet. Create one above.</Typography>
                  </TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      <Dialog open={!!revokeId} onClose={() => setRevokeId(null)}>
        <DialogTitle>Revoke API Key</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to revoke this API key?</Typography>
          <Typography variant="body2" color="error" sx={{ mt: 1 }}>
            Any application using this key will stop working immediately.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRevokeId(null)} disabled={loading.revoke}>Cancel</Button>
          <Button onClick={() => handleRevokeKey(revokeId)} color="error" variant="contained" disabled={loading.revoke}>
            {loading.revoke ? 'Revoking...' : 'Revoke Key'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
