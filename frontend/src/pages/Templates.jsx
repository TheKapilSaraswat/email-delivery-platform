import { useState, useEffect, useRef, useCallback } from 'react';
import {
  Box, Typography, Button, Card, CardContent, CardActions, Grid, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Chip, Alert, IconButton, Tooltip, Divider,
  CircularProgress,
} from '@mui/material';
import { Add, Edit, Delete, Preview, InfoOutlined } from '@mui/icons-material';
import api from '../services/api';

const VARIABLES = [
  { key: '{{name}}', label: 'Full Name', sample: 'John Doe' },
  { key: '{{email}}', label: 'Email', sample: 'john@example.com' },
  { key: '{{first_name}}', label: 'First Name', sample: 'John' },
  { key: '{{last_name}}', label: 'Last Name', sample: 'Doe' },
];

function getSamplePreview(body) {
  let result = body || '';
  VARIABLES.forEach(v => {
    result = result.replaceAll(v.key, v.sample);
  });
  result = result.replaceAll(/\{\{\w+\}\}/g, '');
  return result;
}

export default function Templates() {
  const [templates, setTemplates] = useState([]);
  const [dialog, setDialog] = useState({ open: false, template: null });
  const [preview, setPreview] = useState(null);
  const [deleteId, setDeleteId] = useState(null);
  const [deleteName, setDeleteName] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const bodyRef = useRef(null);

  const fetchTemplates = useCallback(() => {
    setLoading(true);
    api.get('/templates')
      .then(({ data }) => setTemplates(data))
      .catch(() => setError('Failed to load templates'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchTemplates(); }, [fetchTemplates]);

  const handleSave = async () => {
    const t = dialog.template;
    if (!t.name.trim() || !t.subject.trim() || !t.body.trim()) {
      setError('Name, subject, and body are all required');
      return;
    }
    setSaving(true);
    setError('');
    try {
      if (t.id) {
        await api.put(`/templates/${t.id}`, t);
      } else {
        await api.post('/templates', t);
      }
      setDialog({ open: false, template: null });
      fetchTemplates();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save template');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await api.delete(`/templates/${deleteId}`);
      setDeleteId(null);
      setDeleteName('');
      fetchTemplates();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to delete template');
    }
  };

  const insertVariable = (varKey) => {
    const textarea = bodyRef.current;
    const t = dialog.template;
    if (!textarea) {
      setDialog({ ...dialog, template: { ...t, body: t.body + varKey } });
      return;
    }
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const newBody = t.body.substring(0, start) + varKey + t.body.substring(end);
    setDialog({ ...dialog, template: { ...t, body: newBody } });
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + varKey.length, start + varKey.length);
    }, 0);
  };

  return (
    <Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>Templates</Typography>
          <Typography variant="body2" color="textSecondary">
            Create and manage email templates with personalization variables
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />}
          onClick={() => { setError(''); setDialog({ open: true, template: { name: '', subject: '', body: '' } }); }}>
          Create Template
        </Button>
      </Box>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress />
        </Box>
      ) : templates.length === 0 ? (
        <Card sx={{ textAlign: 'center', py: 8 }}>
          <CardContent>
            <Typography variant="h6" color="textSecondary" gutterBottom>No templates yet</Typography>
            <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
              Create your first template to start sending personalized emails
            </Typography>
            <Button variant="contained" startIcon={<Add />}
              onClick={() => setDialog({ open: true, template: { name: '', subject: '', body: '' } })}>
              Create Your First Template
            </Button>
          </CardContent>
        </Card>
      ) : (
        <Grid container spacing={2}>
          {templates.map((t) => (
            <Grid item xs={12} sm={6} md={4} key={t.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" gutterBottom noWrap>{t.name}</Typography>
                  <Typography color="textSecondary" noWrap sx={{ fontSize: 14 }}>
                    Subject: {t.subject}
                  </Typography>
                  <Divider sx={{ my: 1 }} />
                  <Typography variant="body2" color="textSecondary" sx={{ maxHeight: 60, overflow: 'hidden', fontSize: 13 }}>
                    {t.body?.substring(0, 120)}{t.body?.length > 120 ? '...' : ''}
                  </Typography>
                </CardContent>
                <CardActions sx={{ px: 1, pb: 1 }}>
                  <Button size="small" startIcon={<Edit />} onClick={() => { setError(''); setDialog({ open: true, template: t }); }}>Edit</Button>
                  <Button size="small" startIcon={<Preview />} onClick={() => setPreview(t)}>Preview</Button>
                  <Button size="small" color="error" startIcon={<Delete />}
                    onClick={() => { setDeleteId(t.id); setDeleteName(t.name); }}>Delete</Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Dialog open={dialog.open} onClose={() => setDialog({ open: false, template: null })} maxWidth="lg" fullWidth>
        <DialogTitle>{dialog.template?.id ? 'Edit Template' : 'Create Template'}</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            Use variables like <strong>{'{{name}}'}</strong> to personalize emails. Click a variable button to insert it at your cursor position.
          </Alert>

          <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
            {VARIABLES.map((v) => (
              <Tooltip key={v.key} title={`Inserts: ${v.sample}`} arrow>
                <Chip
                  label={v.key}
                  size="small"
                  onClick={() => insertVariable(v.key)}
                  sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'primary.light', color: 'white' } }}
                />
              </Tooltip>
            ))}
          </Box>

          <TextField label="Template Name" fullWidth sx={{ mb: 2 }}
            value={dialog.template?.name || ''}
            onChange={(e) => setDialog({ ...dialog, template: { ...dialog.template, name: e.target.value } })} />

          <TextField label="Email Subject" fullWidth sx={{ mb: 2 }}
            value={dialog.template?.subject || ''}
            onChange={(e) => setDialog({ ...dialog, template: { ...dialog.template, subject: e.target.value } })} />

          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              label="Email Body (HTML)" fullWidth multiline rows={12}
              inputRef={bodyRef}
              value={dialog.template?.body || ''}
              onChange={(e) => setDialog({ ...dialog, template: { ...dialog.template, body: e.target.value } })}
              sx={{ flex: 1 }}
            />
            <Box sx={{ flex: 1 }}>
              <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                Live Preview (Sample Data) <InfoOutlined fontSize="small" color="action" />
              </Typography>
              <Box sx={{
                border: 1, borderColor: 'divider', borderRadius: 1, p: 2, minHeight: 300,
                bgcolor: 'grey.50', overflow: 'auto', maxHeight: 350,
              }}>
                <iframe
                  title="Template Preview"
                  srcDoc={getSamplePreview(dialog.template?.body || '')}
                  sandbox="allow-same-origin"
                  style={{ width: '100%', height: 300, border: 'none', backgroundColor: 'white' }}
                />
              </Box>
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog({ open: false, template: null })} disabled={saving}>Cancel</Button>
          <Button onClick={handleSave} variant="contained" disabled={saving}
            startIcon={saving ? <CircularProgress size={16} color="inherit" /> : null}>
            {saving ? 'Saving...' : 'Save Template'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!preview} onClose={() => setPreview(null)} maxWidth="md" fullWidth>
        <DialogTitle>Preview: {preview?.name}</DialogTitle>
        <DialogContent>
          <Typography variant="subtitle2" gutterBottom>Subject: {preview?.subject}</Typography>
          <Alert severity="info" sx={{ mb: 1 }} icon={<InfoOutlined />}>
            Showing template with sample data. Actual emails use real contact information.
          </Alert>
          <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 2, bgcolor: 'white' }}>
            <iframe
              title="Template Preview"
              srcDoc={getSamplePreview(preview?.body || '')}
              sandbox="allow-same-origin"
              style={{ width: '100%', height: 400, border: 'none' }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPreview(null)}>Close</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!deleteId} onClose={() => { setDeleteId(null); setDeleteName(''); }}>
        <DialogTitle>Delete Template</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete <strong>{deleteName}</strong>?</Typography>
          <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
            This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setDeleteId(null); setDeleteName(''); }}>Cancel</Button>
          <Button onClick={handleDelete} color="error" variant="contained">Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
