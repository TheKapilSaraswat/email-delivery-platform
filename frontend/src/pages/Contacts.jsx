import { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, TextField, Dialog, DialogTitle, DialogContent, DialogActions, IconButton, TablePagination,
  Alert, CircularProgress, Tooltip, Card, CardContent,
} from '@mui/material';
import { Add, Edit, Delete, Upload } from '@mui/icons-material';
import api from '../services/api';

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export default function Contacts() {
  const [contacts, setContacts] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(25);
  const [dialog, setDialog] = useState({ open: false, contact: null });
  const [importDialog, setImportDialog] = useState(false);
  const [importText, setImportText] = useState('');
  const [deleteId, setDeleteId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const fetchContacts = useCallback(() => {
    setLoading(true);
    api.get('/contacts', { params: { search } })
      .then(({ data }) => setContacts(data))
      .catch(() => setError('Failed to load contacts'))
      .finally(() => setLoading(false));
  }, [search]);

  useEffect(() => { fetchContacts(); }, [fetchContacts]);

  const handleSave = async () => {
    const c = dialog.contact;
    const errs = {};
    if (!c.email || !c.email.trim()) {
      errs.email = 'Email is required';
    } else if (!isValidEmail(c.email)) {
      errs.email = 'Please enter a valid email address';
    }
    setFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;

    setActionLoading('save');
    setError('');
    try {
      const payload = { ...c, email: c.email.trim() };
      if (c.id) {
        await api.put(`/contacts/${c.id}`, payload);
      } else {
        await api.post('/contacts', payload);
      }
      setDialog({ open: false, contact: null });
      setFieldErrors({});
      fetchContacts();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save contact');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setActionLoading('delete');
    try {
      await api.delete(`/contacts/${deleteId}`);
      setDeleteId(null);
      fetchContacts();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to delete contact');
    } finally {
      setActionLoading(null);
    }
  };

  const handleImport = async () => {
    if (!importText.trim()) {
      setError('Please paste some contacts to import');
      return;
    }
    setActionLoading('import');
    setError('');
    try {
      const lines = importText.trim().split('\n');
      const parsed = lines.map((line) => {
        const [email, firstName, lastName, list] = line.split(',');
        return { email: email?.trim(), firstName: firstName?.trim(), lastName: lastName?.trim(), list: list?.trim() || 'default' };
      }).filter(c => c.email && isValidEmail(c.email));

      if (parsed.length === 0) {
        setError('No valid emails found. Format: email, firstName, lastName, list');
        setActionLoading(null);
        return;
      }

      const invalidCount = lines.filter(l => l.trim() && !isValidEmail(l.split(',')[0]?.trim())).length;
      await api.post('/contacts/import', parsed);
      setImportDialog(false);
      setImportText('');
      fetchContacts();
      if (invalidCount > 0) {
        setError(`Imported ${parsed.length} contacts. ${invalidCount} lines were skipped (invalid email).`);
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to import contacts');
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <Box>
      {error && <Alert severity={error.includes('Imported') ? 'info' : 'error'} sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>Contacts</Typography>
          <Typography variant="body2" color="textSecondary">
            Manage your contact lists for email campaigns
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button startIcon={<Upload />} onClick={() => { setError(''); setImportDialog(true); }}>Import</Button>
          <Button variant="contained" startIcon={<Add />}
            onClick={() => { setError(''); setDialog({ open: true, contact: { email: '', firstName: '', lastName: '', list: 'default' } }); }}>
            Add Contact
          </Button>
        </Box>
      </Box>

      <TextField
        label="Search by email" size="small" value={search}
        onChange={(e) => { setSearch(e.target.value); setPage(0); }}
        sx={{ mb: 2, width: 300 }}
      />

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>
      ) : contacts.length === 0 ? (
        <Card sx={{ textAlign: 'center', py: 8 }}>
          <CardContent>
            <Typography variant="h6" color="textSecondary" gutterBottom>
              {search ? 'No contacts match your search' : 'No contacts yet'}
            </Typography>
            <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
              {search ? 'Try a different search term' : 'Add contacts or import them from a CSV file'}
            </Typography>
            {!search && (
              <Button variant="contained" startIcon={<Add />}
                onClick={() => setDialog({ open: true, contact: { email: '', firstName: '', lastName: '', list: 'default' } })}>
                Add Your First Contact
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Email</TableCell>
                <TableCell>First Name</TableCell>
                <TableCell>Last Name</TableCell>
                <TableCell>List</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {contacts.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((c) => (
                <TableRow key={c.id} hover>
                  <TableCell>{c.email}</TableCell>
                  <TableCell>{c.firstName || '-'}</TableCell>
                  <TableCell>{c.lastName || '-'}</TableCell>
                  <TableCell>{c.list}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit">
                      <IconButton onClick={() => { setFieldErrors({}); setDialog({ open: true, contact: c }); }} size="small"><Edit /></IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton onClick={() => setDeleteId(c.id)} color="error" size="small"><Delete /></IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div" count={contacts.length} page={page}
            onPageChange={(_, p) => setPage(p)} rowsPerPage={rowsPerPage}
            rowsPerPageOptions={[rowsPerPage]}
          />
        </TableContainer>
      )}

      <Dialog open={dialog.open} onClose={() => { setDialog({ open: false, contact: null }); setFieldErrors({}); }}>
        <DialogTitle>{dialog.contact?.id ? 'Edit Contact' : 'Add Contact'}</DialogTitle>
        <DialogContent>
          <TextField label="Email" fullWidth sx={{ mt: 2 }} value={dialog.contact?.email || ''}
            onChange={(e) => setDialog({ ...dialog, contact: { ...dialog.contact, email: e.target.value } })}
            error={!!fieldErrors.email} helperText={fieldErrors.email} />
          <TextField label="First Name" fullWidth sx={{ mt: 2 }} value={dialog.contact?.firstName || ''}
            onChange={(e) => setDialog({ ...dialog, contact: { ...dialog.contact, firstName: e.target.value } })} />
          <TextField label="Last Name" fullWidth sx={{ mt: 2 }} value={dialog.contact?.lastName || ''}
            onChange={(e) => setDialog({ ...dialog, contact: { ...dialog.contact, lastName: e.target.value } })} />
          <TextField label="List" fullWidth sx={{ mt: 2 }} value={dialog.contact?.list || 'default'}
            onChange={(e) => setDialog({ ...dialog, contact: { ...dialog.contact, list: e.target.value } })}
            helperText="Group contacts into lists for targeted campaigns" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setDialog({ open: false, contact: null }); setFieldErrors({}); }} disabled={actionLoading === 'save'}>Cancel</Button>
          <Button onClick={handleSave} variant="contained" disabled={actionLoading === 'save'}
            startIcon={actionLoading === 'save' ? <CircularProgress size={16} color="inherit" /> : null}>
            {actionLoading === 'save' ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!deleteId} onClose={() => setDeleteId(null)}>
        <DialogTitle>Delete Contact</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete this contact?</Typography>
          <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>This cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteId(null)} disabled={actionLoading === 'delete'}>Cancel</Button>
          <Button onClick={handleDelete} color="error" variant="contained" disabled={actionLoading === 'delete'}>Delete</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={importDialog} onClose={() => setImportDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Import Contacts</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            Paste contacts below, one per line. Format: <strong>email, firstName, lastName, list</strong>
          </Alert>
          <Alert severity="warning" sx={{ mb: 2 }}>
            Lines with invalid emails will be skipped during import.
          </Alert>
          <TextField multiline rows={10} fullWidth value={importText}
            onChange={(e) => setImportText(e.target.value)}
            placeholder={"john@example.com, John, Doe, newsletter\njane@example.com, Jane, Smith, updates\nbob@example.com, Bob, Wilson, default"}
          />
          {importText.trim() && (
            <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
              {importText.trim().split('\n').length} lines entered
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setImportDialog(false)} disabled={actionLoading === 'import'}>Cancel</Button>
          <Button onClick={handleImport} variant="contained" disabled={actionLoading === 'import'}
            startIcon={actionLoading === 'import' ? <CircularProgress size={16} color="inherit" /> : null}>
            {actionLoading === 'import' ? 'Importing...' : 'Import'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
