import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem,
  IconButton, TablePagination, Alert, Tooltip, CircularProgress, Card, CardContent,
} from '@mui/material';
import { Add, Send, Delete, Schedule, Visibility } from '@mui/icons-material';
import api from '../services/api';

const statusColors = { draft: 'default', scheduled: 'info', sending: 'warning', sent: 'success', failed: 'error' };
const statusLabels = {
  draft: 'Draft', scheduled: 'Scheduled', sending: 'Sending...',
  sent: 'Sent', failed: 'Failed',
};

export default function Campaigns() {
  const [campaigns, setCampaigns] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [contacts, setContacts] = useState([]);
  const [dialog, setDialog] = useState({ open: false, campaign: null });
  const [deleteId, setDeleteId] = useState(null);
  const [deleteName, setDeleteName] = useState('');
  const [sendConfirm, setSendConfirm] = useState(null);
  const [scheduleDialog, setScheduleDialog] = useState({ open: false, campaignId: null, datetime: '' });
  const [page, setPage] = useState(0);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [templatePreview, setTemplatePreview] = useState(null);
  const navigate = useNavigate();

  const fetchCampaigns = useCallback(() => {
    return api.get('/campaigns').then(({ data }) => setCampaigns(data)).catch(() => setError('Failed to load campaigns'));
  }, []);

  const fetchTemplates = useCallback(() => {
    return api.get('/templates').then(({ data }) => setTemplates(data)).catch(() => {});
  }, []);

  const fetchContacts = useCallback(() => {
    return api.get('/contacts').then(({ data }) => setContacts(data)).catch(() => {});
  }, []);

  useEffect(() => {
    Promise.all([fetchCampaigns(), fetchTemplates(), fetchContacts()])
      .finally(() => setLoading(false));
  }, [fetchCampaigns, fetchTemplates, fetchContacts]);

  const handleSave = async () => {
    const c = dialog.campaign;
    if (!c.name.trim()) {
      setError('Campaign name is required');
      return;
    }
    setActionLoading('save');
    setError('');
    try {
      if (c.id) {
        await api.put(`/campaigns/${c.id}`, c);
      } else {
        await api.post('/campaigns', c);
      }
      setDialog({ open: false, campaign: null });
      fetchCampaigns();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save campaign');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setActionLoading('delete');
    try {
      await api.delete(`/campaigns/${deleteId}`);
      setDeleteId(null);
      setDeleteName('');
      fetchCampaigns();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to delete campaign');
    } finally {
      setActionLoading(null);
    }
  };

  const handleSend = async (id) => {
    setActionLoading('send-' + id);
    setError('');
    try {
      await api.post(`/campaigns/${id}/send`);
      setSendConfirm(null);
      fetchCampaigns();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to send campaign');
    } finally {
      setActionLoading(null);
    }
  };

  const handleSchedule = async () => {
    if (!scheduleDialog.datetime) {
      setError('Please select a date and time');
      return;
    }
    setActionLoading('schedule');
    setError('');
    try {
      const dt = new Date(scheduleDialog.datetime);
      const iso = dt.toISOString().replace('Z', '').split('.')[0];
      await api.post(`/campaigns/${scheduleDialog.campaignId}/schedule`, { scheduledAt: iso });
      setScheduleDialog({ open: false, campaignId: null, datetime: '' });
      fetchCampaigns();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to schedule campaign');
    } finally {
      setActionLoading(null);
    }
  };

  const getContactCount = (listName) => {
    if (!listName) return contacts.length;
    return contacts.filter(c => c.list === listName).length;
  };

  const getTemplateName = (templateId) => {
    if (!templateId) return 'None';
    const t = templates.find(t => t.id === templateId);
    return t ? t.name : 'Unknown';
  };

  return (
    <Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>Campaigns</Typography>
          <Typography variant="body2" color="textSecondary">
            Create, send, and track your email campaigns
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />}
          onClick={() => { setError(''); setDialog({ open: true, campaign: { name: '', description: '', templateId: '', contactList: '' } }); }}>
          New Campaign
        </Button>
      </Box>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>
      ) : campaigns.length === 0 ? (
        <Card sx={{ textAlign: 'center', py: 8 }}>
          <CardContent>
            <Typography variant="h6" color="textSecondary" gutterBottom>No campaigns yet</Typography>
            <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
              Create your first campaign to start sending emails
            </Typography>
            <Button variant="contained" startIcon={<Add />}
              onClick={() => setDialog({ open: true, campaign: { name: '', description: '', templateId: '', contactList: '' } })}>
              Create Your First Campaign
            </Button>
          </CardContent>
        </Card>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Campaign</TableCell>
                <TableCell>Template</TableCell>
                <TableCell>Contact List</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="center">Sent</TableCell>
                <TableCell align="center">Opened</TableCell>
                <TableCell align="center">Clicked</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {campaigns.slice(page * 25, page * 25 + 25).map((c) => (
                <TableRow key={c.id} hover style={{ cursor: 'pointer' }} onClick={() => navigate(`/campaigns/${c.id}`)}>
                  <TableCell>
                    <Typography fontWeight={500}>{c.name}</Typography>
                    {c.description && (
                      <Typography variant="body2" color="textSecondary" noWrap sx={{ maxWidth: 200 }}>
                        {c.description}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">{getTemplateName(c.templateId)}</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">{c.contactList || 'All contacts'}</Typography>
                    <Typography variant="caption" color="textSecondary">
                      {getContactCount(c.contactList)} contacts
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip label={statusLabels[c.status] || c.status} color={statusColors[c.status] || 'default'} size="small" />
                  </TableCell>
                  <TableCell align="center">{c.sentCount || 0}</TableCell>
                  <TableCell align="center">{c.openedCount || 0}</TableCell>
                  <TableCell align="center">{c.clickedCount || 0}</TableCell>
                  <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                    {(c.status === 'draft' || c.status === 'scheduled') && (
                      <>
                        <Tooltip title="Send Now">
                          <IconButton onClick={() => setSendConfirm(c)}
                            disabled={actionLoading === 'send-' + c.id} size="small">
                            <Send color="primary" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Schedule">
                          <IconButton onClick={() => setScheduleDialog({ open: true, campaignId: c.id, datetime: '' })}
                            disabled={actionLoading === 'schedule'} size="small">
                            <Schedule color="info" />
                          </IconButton>
                        </Tooltip>
                      </>
                    )}
                    {c.status === 'sent' && (
                      <Tooltip title="View Analytics">
                        <IconButton onClick={() => navigate(`/campaigns/${c.id}`)} size="small">
                          <Visibility color="primary" />
                        </IconButton>
                      </Tooltip>
                    )}
                    {(c.status === 'draft' || c.status === 'scheduled') && (
                      <Tooltip title="Delete">
                        <IconButton onClick={() => { setDeleteId(c.id); setDeleteName(c.name); }}
                          disabled={actionLoading === 'delete'} size="small" color="error">
                          <Delete />
                        </IconButton>
                      </Tooltip>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div" count={campaigns.length} page={page}
            onPageChange={(_, p) => setPage(p)} rowsPerPage={25} rowsPerPageOptions={[25]}
          />
        </TableContainer>
      )}

      <Dialog open={dialog.open} onClose={() => setDialog({ open: false, campaign: null })} maxWidth="sm" fullWidth>
        <DialogTitle>{dialog.campaign?.id ? 'Edit Campaign' : 'Create Campaign'}</DialogTitle>
        <DialogContent>
          <TextField label="Campaign Name" fullWidth sx={{ mt: 2 }} value={dialog.campaign?.name || ''}
            onChange={(e) => setDialog({ ...dialog, campaign: { ...dialog.campaign, name: e.target.value } })} />
          <TextField label="Description (optional)" fullWidth sx={{ mt: 2 }} value={dialog.campaign?.description || ''}
            onChange={(e) => setDialog({ ...dialog, campaign: { ...dialog.campaign, description: e.target.value } })}
            helperText="Brief description for your reference" />

          <TextField select label="Email Template" fullWidth sx={{ mt: 2 }} value={dialog.campaign?.templateId || ''}
            onChange={(e) => setDialog({ ...dialog, campaign: { ...dialog.campaign, templateId: e.target.value } })}>
            <MenuItem value="">
              <em>No template (subject only)</em>
            </MenuItem>
            {templates.map((t) => (
              <MenuItem key={t.id} value={t.id}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                  <span>{t.name}</span>
                  <Tooltip title="Preview" arrow>
                    <IconButton size="small" onClick={(e) => { e.stopPropagation(); setTemplatePreview(t); }}>
                      <Visibility fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Box>
              </MenuItem>
            ))}
          </TextField>

          <TextField label="Contact List" fullWidth sx={{ mt: 2 }} value={dialog.campaign?.contactList || ''}
            onChange={(e) => setDialog({ ...dialog, campaign: { ...dialog.campaign, contactList: e.target.value } })}
            helperText={dialog.campaign?.contactList
              ? `${getContactCount(dialog.campaign.contactList)} contacts in "${dialog.campaign.contactList}"`
              : 'Leave empty to send to all contacts'}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog({ open: false, campaign: null })} disabled={actionLoading === 'save'}>Cancel</Button>
          <Button onClick={handleSave} variant="contained" disabled={actionLoading === 'save'}
            startIcon={actionLoading === 'save' ? <CircularProgress size={16} color="inherit" /> : null}>
            {actionLoading === 'save' ? 'Saving...' : 'Save Campaign'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!sendConfirm} onClose={() => setSendConfirm(null)}>
        <DialogTitle>Send Campaign</DialogTitle>
        <DialogContent>
          <Typography gutterBottom>
            Are you sure you want to send <strong>{sendConfirm?.name}</strong>?
          </Typography>
          {sendConfirm && (
            <Alert severity="info" sx={{ mt: 1 }}>
              This will send to <strong>{getContactCount(sendConfirm.contactList)}</strong> contacts
              using template <strong>{getTemplateName(sendConfirm.templateId)}</strong>.
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSendConfirm(null)} disabled={actionLoading === 'send-' + sendConfirm?.id}>Cancel</Button>
          <Button onClick={() => handleSend(sendConfirm?.id)} variant="contained" color="warning"
            disabled={actionLoading === 'send-' + sendConfirm?.id}
            startIcon={actionLoading === 'send-' + sendConfirm?.id ? <CircularProgress size={16} color="inherit" /> : null}>
            {actionLoading === 'send-' + sendConfirm?.id ? 'Sending...' : 'Yes, Send Now'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={scheduleDialog.open} onClose={() => setScheduleDialog({ open: false, campaignId: null, datetime: '' })}>
        <DialogTitle>Schedule Campaign</DialogTitle>
        <DialogContent>
          <Typography gutterBottom>Select when this campaign should be sent:</Typography>
          <TextField
            type="datetime-local" fullWidth sx={{ mt: 2 }}
            value={scheduleDialog.datetime}
            onChange={(e) => setScheduleDialog({ ...scheduleDialog, datetime: e.target.value })}
            InputLabelProps={{ shrink: true }}
            label="Send at"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setScheduleDialog({ open: false, campaignId: null, datetime: '' })} disabled={actionLoading === 'schedule'}>Cancel</Button>
          <Button onClick={handleSchedule} variant="contained" disabled={actionLoading === 'schedule'}
            startIcon={actionLoading === 'schedule' ? <CircularProgress size={16} color="inherit" /> : null}>
            {actionLoading === 'schedule' ? 'Scheduling...' : 'Schedule'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!deleteId} onClose={() => { setDeleteId(null); setDeleteName(''); }}>
        <DialogTitle>Delete Campaign</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete <strong>{deleteName}</strong>?</Typography>
          <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>This cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setDeleteId(null); setDeleteName(''); }} disabled={actionLoading === 'delete'}>Cancel</Button>
          <Button onClick={handleDelete} color="error" variant="contained" disabled={actionLoading === 'delete'}>Delete</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!templatePreview} onClose={() => setTemplatePreview(null)} maxWidth="md" fullWidth>
        <DialogTitle>Template Preview: {templatePreview?.name}</DialogTitle>
        <DialogContent>
          <Typography variant="subtitle2" gutterBottom>Subject: {templatePreview?.subject}</Typography>
          <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 2, bgcolor: 'white' }}>
            <iframe
              title="Template Preview"
              srcDoc={(() => {
                let body = templatePreview?.body || '';
                const vars = [
                  { k: '{{name}}', v: 'John Doe' }, { k: '{{email}}', v: 'john@example.com' },
                  { k: '{{first_name}}', v: 'John' }, { k: '{{last_name}}', v: 'Doe' },
                ];
                vars.forEach(({ k, v }) => { body = body.replaceAll(k, v); });
                body = body.replaceAll(/\{\{\w+\}\}/g, '');
                return body;
              })()}
              sandbox="allow-same-origin"
              style={{ width: '100%', height: 400, border: 'none' }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTemplatePreview(null)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
