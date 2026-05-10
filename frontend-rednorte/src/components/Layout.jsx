import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout({ children }) {
  const { usuario, cerrarSesion, esAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    cerrarSesion();
    navigate('/login');
  };

  const nombre = usuario?.persona
    ? `${usuario.persona.apellido1 || ''} ${usuario.persona.apellido2 || ''}`.trim()
    : usuario?.mail || 'Usuario';

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <h1>RedNorte</h1>
          <span>Gestión de espera</span>
        </div>

        <nav>
          {/* Navegación del paciente */}
          {!esAdmin && (
            <>
              <NavLink to="/mis-consultas"  className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
                Mis consultas
              </NavLink>
              <NavLink to="/nueva-consulta" className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
                Nueva consulta
              </NavLink>
            </>
          )}

          {/* Navegación del administrador */}
          {esAdmin && (
            <>
              <NavLink to="/admin/dashboard"    className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
                Panel de control
              </NavLink>
              <NavLink to="/admin/consultas"    className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
                Todas las consultas
              </NavLink>
              <NavLink to="/admin/usuarios"     className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
                Gestión de usuarios
              </NavLink>
              <NavLink to="/admin/reasignacion" className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}>
                Reasignación
              </NavLink>
            </>
          )}
        </nav>

        <div className="sidebar-footer">
          <div style={{ marginBottom: 4, fontWeight: 500, color: 'white', fontSize: 14 }}>
            {nombre}
          </div>
          <div style={{ fontSize: 11, marginBottom: 12, opacity: 0.6 }}>
            {esAdmin ? 'Administrador' : 'Paciente'}
          </div>
          <button
            onClick={handleLogout}
            className="btn btn-secundario btn-sm"
            style={{ width: '100%', justifyContent: 'center' }}
          >
            Cerrar sesión
          </button>
        </div>
      </aside>

      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
