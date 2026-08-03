import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, ToggleButtonGroup, ToggleButton,
  CircularProgress, IconButton, InputAdornment,
} from '@mui/material';
import { Visibility, VisibilityOff, MailOutlined, LockOutlined, PersonOutlined } from '@mui/icons-material';
import api from '../services/api';

export default function Auth() {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ email: '', password: '', name: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const validate = () => {
    const errs = {};
    if (!form.email.trim()) {
      errs.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      errs.email = 'Please enter a valid email address';
    }
    if (!form.password) {
      errs.password = 'Password is required';
    } else if (form.password.length < 6) {
      errs.password = 'Password must be at least 6 characters';
    }
    if (mode === 'register') {
      if (!form.name.trim()) {
        errs.name = 'Name is required';
      }
      if (!form.confirmPassword) {
        errs.confirmPassword = 'Please confirm your password';
      } else if (form.password !== form.confirmPassword) {
        errs.confirmPassword = 'Passwords do not match';
      }
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    setSuccessMsg('');
    if (!validate()) return;
    setLoading(true);
    try {
      const endpoint = mode === 'login' ? '/auth/login' : '/auth/register';
      const payload = mode === 'register'
        ? { email: form.email.trim(), password: form.password, name: form.name.trim() }
        : { email: form.email.trim(), password: form.password };
      const { data } = await api.post(endpoint, payload);
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify({ id: data.userId, email: data.email, name: data.name }));
      if (mode === 'register') {
        setSuccessMsg('Account created successfully! Redirecting...');
      }
      setTimeout(() => navigate('/'), mode === 'register' ? 1000 : 0);
    } catch (err) {
      const resp = err.response?.data;
      if (resp?.fields) {
        const fieldErrors = {};
        Object.entries(resp.fields).forEach(([field, msg]) => { fieldErrors[field] = msg; });
        setErrors(fieldErrors);
        setServerError('');
      } else if (resp?.error) {
        setServerError(resp.error);
      } else if (err.response?.status === 409) {
        setServerError('An account with this email already exists. Try logging in instead.');
      } else if (err.response?.status === 401) {
        setServerError('Invalid email or password. Please try again.');
      } else if (err.response?.status >= 500) {
        setServerError('Server error. Please try again later.');
      } else if (err.code === 'ECONNREFUSED') {
        setServerError('Cannot connect to server. Please make sure the backend is running.');
      } else if (err.code === 'ERR_NETWORK') {
        setServerError('Network error. Please check your connection and try again.');
      } else {
        setServerError('Something went wrong. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = (_, v) => {
    if (!v || loading) return;
    setMode(v);
    setErrors({});
    setServerError('');
    setSuccessMsg('');
    setForm({ email: form.email, password: '', name: '', confirmPassword: '' });
  };

  const fieldError = (field) => errors[field] ? (
    <Typography variant="caption" color="error" sx={{ mt: -1, mb: 1, display: 'block' }}>
      {errors[field]}
    </Typography>
  ) : null;

  return (
    <Box sx={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'linear-gradient(135deg, #1976d2 0%, #9c27b0 100%)',
    }}>
      <Card sx={{ width: 420, p: 2, borderRadius: 3 }}>
        <CardContent>
          <Box sx={{ textAlign: 'center', mb: 2 }}>
            <Typography variant="h5" fontWeight={700}>
              Email Delivery Platform
            </Typography>
            <Typography variant="body2" color="textSecondary">
              {mode === 'login' ? 'Sign in to your account' : 'Create a new account'}
            </Typography>
          </Box>

          <ToggleButtonGroup value={mode} exclusive onChange={handleToggle} fullWidth sx={{ mb: 3 }}>
            <ToggleButton value="login" disabled={loading}>Login</ToggleButton>
            <ToggleButton value="register" disabled={loading}>Register</ToggleButton>
          </ToggleButtonGroup>

          {serverError && <Alert severity="error" sx={{ mb: 2 }}>{serverError}</Alert>}
          {successMsg && <Alert severity="success" sx={{ mb: 2 }}>{successMsg}</Alert>}

          <Box component="form" onSubmit={handleSubmit} noValidate>
            {mode === 'register' && (
              <>
                <TextField
                  label="Full Name" fullWidth sx={{ mb: 0.5 }}
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  error={!!errors.name}
                  disabled={loading}
                  InputProps={{
                    startAdornment: <InputAdornment position="start"><PersonOutlined /></InputAdornment>,
                  }}
                />
                {fieldError('name')}
              </>
            )}

            <TextField
              label="Email" type="email" fullWidth sx={{ mb: 0.5 }}
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              error={!!errors.email}
              disabled={loading}
              InputProps={{
                startAdornment: <InputAdornment position="start"><MailOutlined /></InputAdornment>,
              }}
            />
            {fieldError('email')}

            <TextField
              label="Password" fullWidth sx={{ mb: 0.5 }}
              type={showPassword ? 'text' : 'password'}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              error={!!errors.password}
              disabled={loading}
              InputProps={{
                startAdornment: <InputAdornment position="start"><LockOutlined /></InputAdornment>,
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" size="small">
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            {fieldError('password')}

            {mode === 'register' && (
              <>
                <TextField
                  label="Confirm Password" fullWidth sx={{ mb: 0.5 }}
                  type={showPassword ? 'text' : 'password'}
                  value={form.confirmPassword}
                  onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                  error={!!errors.confirmPassword}
                  disabled={loading}
                  InputProps={{
                    startAdornment: <InputAdornment position="start"><LockOutlined /></InputAdornment>,
                  }}
                />
                {fieldError('confirmPassword')}
              </>
            )}

            <Button
              type="submit" variant="contained" fullWidth
              disabled={loading}
              sx={{ mt: 2, py: 1.5, fontWeight: 600 }}
              startIcon={loading ? <CircularProgress size={20} color="inherit" /> : null}
            >
              {loading
                ? (mode === 'login' ? 'Signing in...' : 'Creating account...')
                : (mode === 'login' ? 'Sign In' : 'Create Account')
              }
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
