import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import './index.css';

import LoginPage            from './pages/LoginPage';
import MisConsultasPage     from './pages/MisConsultasPage';
import NuevaConsultaPage    from './pages/NuevaConsultaPage';
import EditarConsultaPage   from './pages/EditarConsultaPage';
import AdminDashboardPage   from './pages/AdminDashboardPage';
import AdminConsultasPage   from './pages/AdminConsultasPage';
import AdminUsuariosPage    from './pages/AdminUsuariosPage';
import AdminReasignacionPage from './pages/AdminReasignacionPage';

function RutaProtegida({ children, soloAdmin = false }) {
  const { usuario, esAdmin } = useAuth();
  if (!usuario) return <Navigate to="/login" replace />;
  if (soloAdmin && !esAdmin) return <Navigate to="/mis-consultas" replace />;
  return children;
}

function RutaPublica({ children }) {
  const { usuario, esAdmin } = useAuth();
  if (usuario) return <Navigate to={esAdmin ? '/admin/dashboard' : '/mis-consultas'} replace />;
  return children;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<RutaPublica><LoginPage /></RutaPublica>} />

      {/* Rutas del paciente */}
      <Route path="/mis-consultas"      element={<RutaProtegida><MisConsultasPage /></RutaProtegida>} />
      <Route path="/nueva-consulta"     element={<RutaProtegida><NuevaConsultaPage /></RutaProtegida>} />
      <Route path="/editar-consulta/:id" element={<RutaProtegida><EditarConsultaPage /></RutaProtegida>} />

      {/* Rutas del admin */}
      <Route path="/admin/dashboard"    element={<RutaProtegida soloAdmin><AdminDashboardPage /></RutaProtegida>} />
      <Route path="/admin/consultas"    element={<RutaProtegida soloAdmin><AdminConsultasPage /></RutaProtegida>} />
      <Route path="/admin/usuarios"     element={<RutaProtegida soloAdmin><AdminUsuariosPage /></RutaProtegida>} />
      <Route path="/admin/reasignacion" element={<RutaProtegida soloAdmin><AdminReasignacionPage /></RutaProtegida>} />

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
