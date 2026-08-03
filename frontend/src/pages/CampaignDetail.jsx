import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, Typography, Paper, Grid, Card, CardContent, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Button, LinearProgress, CircularProgress, Alert,
  Tooltip, TablePagination,
} from '@mui/material';
import { ArrowBack, InfoOutlined, Refresh } from '@mui/icons-material';
import api from '../services/api';

function formatTimestamp(ts) {
  if (!ts) return '-';
  try {
    const date = new Date(ts);
    const now = new Date();
    const diffMs = now - date;
    const diffMin = Math.floor(diffMs / 60000);
    const diffHr = Math.floor(diffMs / 3600000);
    const diffDay = Math.floor(diffMs / 86400000);
    if (diffMin < 1) return 'Just now';
    if (diffMin < 60) return `${diffMin}m ago`;
    if (diffHr < 24) return `${diffHr}h ago`;
    if (diffDay < 7) return `${diffDay}d ago`;
    return date.toLocaleDateString();
  } catch {
    return ts;
  }
}

function StatCard({ label, value, rate, rateLabel, color, tooltipText }) {
  return (
    <Grid item xs={6} sm={3}>
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <Typography color="textSecondary" variant="body2">{label}</Typography>
            {tooltipText && (
              <Tooltip title={tooltipText} arrow>
                <InfoOutlined fontSize="small" color="action" />
              </Tooltip>
            )}
          </Box>
          <Typography variant="h5" fontWeight={700}>{value}</Typography>
          {rate !== undefined && (
            <>
              <LinearProgress
                variant="determinate"
                value={Math.min(rate, 100)}
                color={color}
                sx={{ mt: 1, height: 6, borderRadius: 3 }}
              />
              <Typography variant="caption" color="textSecondary">{rateLabel}: {rate}%</Typography>
            </>
          )}
        </CardContent>
      </Card>
    </Grid>
  );
}

export default function CampaignDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [eventPage, setEventPage] = useState(0);
  const [eventFilter, setEventFilter] = useState('all');
  const eventsPerPage = 15;

  const fetchData = useCallback(() => {
    setLoading(true);
    setError('');
    api.get(`/analytics/campaigns/${id}`)
      .then(({ data }) => setData(data))
      .catch(() => setError('Failed to load campaign analytics'))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => { fetchData(); }, [fetchData]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 12 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ py: 4 }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/campaigns')} sx={{ mb: 2 }}>Back to Campaigns</Button>
        <Alert severity="error" action={<Button color="inherit" onClick={fetchData}>Retry</Button>}>{error}</Alert>
      </Box>
    );
  }

  const { campaign, stats, rates, events } = data;

  const filteredEvents = eventFilter === 'all' ? events : events.filter(e => e.event === eventFilter);
  const paginatedEvents = filteredEvents.slice(eventPage * eventsPerPage, eventPage * eventsPerPage + eventsPerPage);

  const eventTypes = ['all', ...new Set(events.map(e => e.event))];
  const eventColors = { open: 'primary', click: 'secondary', sent: 'default' };

  return (
    <Box>
      <Button startIcon={<ArrowBack />} onClick={() => navigate('/campaigns')} sx={{ mb: 2 }}>Back to Campaigns</Button>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>{campaign.name}</Typography>
          {campaign.description && (
            <Typography variant="body2" color="textSecondary">{campaign.description}</Typography>
          )}
        </Box>
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <Chip label={campaign.status} color={campaign.status === 'sent' ? 'success' : 'default'} />
          <Button startIcon={<Refresh />} onClick={fetchData} size="small">Refresh</Button>
        </Box>
      </Box>

      <Alert severity="info" icon={<InfoOutlined />} sx={{ mb: 3 }}>
        <strong>How to read these metrics:</strong> An "open" is recorded when a recipient's email client loads the tracking pixel.
        A "click" is recorded when a recipient clicks any link in the email.
      </Alert>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <StatCard label="Sent" value={stats.sent} color="primary" tooltipText="Total emails successfully sent" />
        <StatCard label="Opened" value={stats.opened} rate={rates.open_rate} rateLabel="Open rate" color="primary"
          tooltipText="Recipients who opened the email (tracking pixel loaded)" />
        <StatCard label="Clicked" value={stats.clicked} rate={rates.click_rate} rateLabel="Click rate" color="secondary"
          tooltipText="Recipients who clicked at least one link" />
        <StatCard label="Click-to-Open" value={`${rates.click_to_open_rate}%`} color="secondary"
          tooltipText="Percentage of openers who also clicked a link" />
      </Grid>

      <Paper sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          <Typography variant="h6">Event Timeline</Typography>
          <Box sx={{ display: 'flex', gap: 0.5 }}>
            {eventTypes.map(type => (
              <Chip
                key={type}
                label={type === 'all' ? 'All' : type}
                size="small"
                color={eventFilter === type ? (eventColors[type] || 'primary') : 'default'}
                variant={eventFilter === type ? 'filled' : 'outlined'}
                onClick={() => { setEventFilter(type); setEventPage(0); }}
                sx={{ cursor: 'pointer' }}
              />
            ))}
          </Box>
        </Box>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Email</TableCell>
                <TableCell>Event</TableCell>
                <TableCell>Time</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginatedEvents.map((e) => (
                <TableRow key={e.id}>
                  <TableCell>{e.email}</TableCell>
                  <TableCell>
                    <Chip label={e.event} size="small" color={eventColors[e.event] || 'default'} />
                  </TableCell>
                  <TableCell>
                    <Tooltip title={e.timestamp || ''} arrow>
                      <span>{formatTimestamp(e.timestamp)}</span>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
              {filteredEvents.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3} align="center" sx={{ py: 4 }}>
                    <Typography color="textSecondary">
                      {eventFilter === 'all' ? 'No events recorded yet' : `No ${eventFilter} events`}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        {filteredEvents.length > eventsPerPage && (
          <TablePagination
            component="div"
            count={filteredEvents.length}
            page={eventPage}
            onPageChange={(_, p) => setEventPage(p)}
            rowsPerPage={eventsPerPage}
            rowsPerPageOptions={[eventsPerPage]}
          />
        )}
      </Paper>
    </Box>
  );
}
