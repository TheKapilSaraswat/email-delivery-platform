import { Navigate, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout.jsx'
import Auth from './pages/Auth.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Campaigns from './pages/Campaigns.jsx'
import CampaignDetail from './pages/CampaignDetail.jsx'
import Templates from './pages/Templates.jsx'
import Contacts from './pages/Contacts.jsx'
import Settings from './pages/Settings.jsx'

function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token')
  if (!token) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Auth />} />
      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="campaigns" element={<Campaigns />} />
        <Route path="campaigns/:id" element={<CampaignDetail />} />
        <Route path="templates" element={<Templates />} />
        <Route path="contacts" element={<Contacts />} />
        <Route path="settings" element={<Settings />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
