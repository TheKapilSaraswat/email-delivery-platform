import { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  Box, Drawer, AppBar, Toolbar, Typography, List, ListItem, ListItemButton, ListItemIcon,
  ListItemText, Avatar, Menu, MenuItem, IconButton, Button, useMediaQuery, useTheme, Divider,
} from '@mui/material';
import {
  Dashboard, Email, Description, People, Settings, Logout, Menu as MenuIcon,
} from '@mui/icons-material';

const drawerWidth = 240;

const navItems = [
  { path: '/', label: 'Dashboard', icon: <Dashboard /> },
  { path: '/campaigns', label: 'Campaigns', icon: <Email /> },
  { path: '/templates', label: 'Templates', icon: <Description /> },
  { path: '/contacts', label: 'Contacts', icon: <People /> },
  { path: '/settings', label: 'Settings', icon: <Settings /> },
];

function SafeAvatar({ user }) {
  let name = 'U';
  try {
    if (user && typeof user === 'object' && user.name) {
      name = user.name[0] || 'U';
    }
  } catch {}
  return (
    <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
      {name}
    </Avatar>
  );
}

function SafeUserName({ user }) {
  try {
    if (user && typeof user === 'object' && user.name) return user.name;
  } catch {}
  return 'User';
}

export default function Layout() {
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);

  let user = {};
  try {
    user = JSON.parse(localStorage.getItem('user') || '{}');
  } catch {
    user = {};
  }

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const drawerContent = (
    <>
      <Toolbar />
      <List sx={{ px: 1 }}>
        {navItems.map((item) => (
          <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
            <ListItemButton
              component={NavLink}
              to={item.path}
              end={item.path === '/'}
              onClick={() => isMobile && setMobileOpen(false)}
              sx={{
                borderRadius: 2,
                '&.active': {
                  bgcolor: 'primary.main', color: 'white',
                  '& .MuiListItemIcon-root': { color: 'white' },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Box sx={{ flexGrow: 1 }} />
      <List sx={{ px: 1, pb: 2 }}>
        <ListItem disablePadding>
          <ListItemButton onClick={handleLogout} sx={{ borderRadius: 2 }}>
            <ListItemIcon sx={{ minWidth: 40 }}><Logout /></ListItemIcon>
            <ListItemText primary="Logout" />
          </ListItemButton>
        </ListItem>
      </List>
    </>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          {isMobile && (
            <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}>
              <MenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" noWrap sx={{ flexGrow: 1 }}>
            Email Delivery Platform
          </Typography>
          <Button color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}
            startIcon={<SafeAvatar user={user} />}>
            <SafeUserName user={user} />
          </Button>
          <Menu anchorEl={anchorEl} open={!!anchorEl} onClose={() => setAnchorEl(null)}>
            <MenuItem onClick={() => { setAnchorEl(null); navigate('/settings'); }}>Settings</MenuItem>
            <Divider />
            <MenuItem onClick={() => { setAnchorEl(null); handleLogout(); }} sx={{ color: 'error.main' }}>
              <ListItemIcon><Logout fontSize="small" /></ListItemIcon>
              Logout
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      {isMobile ? (
        <Drawer variant="temporary" open={mobileOpen} onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box' },
          }}
        >
          {drawerContent}
        </Drawer>
      ) : (
        <Drawer variant="permanent" sx={{
          width: drawerWidth, flexShrink: 0,
          '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box' },
        }}>
          {drawerContent}
        </Drawer>
      )}

      <Box component="main" sx={{ flexGrow: 1, p: 3, minHeight: '100vh' }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}
