import { useState, useEffect, useCallback } from 'react';
import {
  Box, Grid, Card, CardContent, Typography, Paper, CircularProgress, Alert, Button,
  LinearProgress, Divider, Chip,
} from '@mui/material';
import { Refresh, Email, People, TrendingUp, BarChart, InfoOutlined } from '@mui/icons-material';
import api from '../services/api';

function formatNumber(n) {
  if (n === undefined || n === null) return '0';
  return n.toLocaleString();
}

function StatCard({ label, value, color, icon, subtitle }) {
  return (
    <Card sx={{ height: '100%', borderLeft: 4, borderColor: color }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Box>
            <Typography color="textSecondary" variant="body2" gutterBottom>{label}</Typography>
            <Typography variant="h4" fontWeight={700}>{value}</Typography>
            {subtitle && <Typography variant="caption" color="textSecondary">{subtitle}</Typography>}
          </Box>
          <Box sx={{ color: color, opacity: 0.3 }}>{icon}</Box>
        </Box>
      </CardContent>
    </Card>
  );
}

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = useCallback(() => {
    setLoading(true);
    setError('');
    api.get('/analytics/overview')
      .then(({ data }) => setData(data))
      .catch(() => setError('Failed to load dashboard data'))
      .finally(() => setLoading(false));
  }, []);

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
        <Alert severity="error" action={<Button color="inherit" onClick={fetchData}>Retry</Button>}>{error}</Alert>
      </Box>
    );
  }

  const stats = [
    { label: 'Total Campaigns', value: formatNumber(data?.totalCampaigns), color: '#1976d2', icon: <Email sx={{ fontSize: 40 }} /> },
    { label: 'Emails Sent', value: formatNumber(data?.totalSent), color: '#388e3c', icon: <BarChart sx={{ fontSize: 40 }} /> },
    { label: 'Total Opens', value: formatNumber(data?.totalOpens), color: '#f57c00', icon: <TrendingUp sx={{ fontSize: 40 }} /> },
    { label: 'Total Clicks', value: formatNumber(data?.totalClicks), color: '#9c27b0', icon: <People sx={{ fontSize: 40 }} /> },
  ];

  const openRate = data?.openRate || 0;
  const clickRate = data?.clickRate || 0;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>Dashboard</Typography>
          <Typography variant="body2" color="textSecondary">Your email marketing performance at a glance</Typography>
        </Box>
        <Button startIcon={<Refresh />} onClick={fetchData}>Refresh</Button>
      </Box>

      <Grid container spacing={3}>
        {stats.map((s) => (
          <Grid item xs={12} sm={6} md={3} key={s.label}>
            <StatCard {...s} />
          </Grid>
        ))}
      </Grid>

      <Paper sx={{ mt: 3, p: 3 }}>
        <Typography variant="h6" gutterBottom fontWeight={600}>Performance Metrics</Typography>
        <Alert severity="info" icon={<InfoOutlined />} sx={{ mb: 2 }}>
          <strong>Open Rate</strong> = emails opened / emails sent. <strong>Click Rate</strong> = links clicked / emails sent.
        </Alert>

        <Grid container spacing={3}>
          <Grid item xs={12} sm={6}>
            <Box sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
              <Typography variant="subtitle2" color="textSecondary" gutterBottom>Open Rate</Typography>
              <Typography variant="h3" fontWeight={700} color="primary">{openRate}%</Typography>
              <LinearProgress variant="determinate" value={Math.min(openRate, 100)} sx={{ mt: 1, height: 8, borderRadius: 4 }} />
              <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
                {formatNumber(data?.totalOpens)} opens out of {formatNumber(data?.totalSent)} emails sent
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Box sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
              <Typography variant="subtitle2" color="textSecondary" gutterBottom>Click Rate</Typography>
              <Typography variant="h3" fontWeight={700} color="secondary">{clickRate}%</Typography>
              <LinearProgress variant="determinate" value={Math.min(clickRate, 100)} color="secondary" sx={{ mt: 1, height: 8, borderRadius: 4 }} />
              <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
                {formatNumber(data?.totalClicks)} clicks out of {formatNumber(data?.totalSent)} emails sent
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ mt: 3, p: 3 }}>
        <Typography variant="h6" gutterBottom fontWeight={600}>How Tracking Works</Typography>
        <Divider sx={{ mb: 2 }} />
        <Grid container spacing={2}>
          <Grid item xs={12} sm={4}>
            <Chip label="1" color="primary" size="small" sx={{ mb: 1 }} />
            <Typography variant="subtitle2">Open Tracking</Typography>
            <Typography variant="body2" color="textSecondary">
              A tiny invisible image is embedded in each email. When the recipient opens the email, their email client loads this image, recording an "open" event.
            </Typography>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Chip label="2" color="primary" size="small" sx={{ mb: 1 }} />
            <Typography variant="subtitle2">Click Tracking</Typography>
            <Typography variant="body2" color="textSecondary">
              Links in your emails are rewritten to pass through our tracking server. When a recipient clicks a link, we record the click and redirect them to the original URL.
            </Typography>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Chip label="3" color="primary" size="small" sx={{ mb: 1 }} />
            <Typography variant="subtitle2">Analytics</Typography>
            <Typography variant="body2" color="textSecondary">
              All events are collected and displayed here. You can see open rates, click rates, and per-campaign performance to measure your email engagement.
            </Typography>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
}
