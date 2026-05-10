import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { listarUsuarios, listarRoles, crearUsuario } from '../services/api';

export default function AdminUsuariosPage() {
  const [usuarios, setUsuarios] = useState([]);
  const [roles, setRoles] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [mostrarForm, setMostrarForm] = useState(false);
  const [form, setForm] = useState({ id: '', mail: '', pass: '', estado: 'ACTIVO', rolId: '' });
  const [creando, setCreando] = useState(false);

  const cargar = () => {
    setCargando(true);
    Promise.all([listarUsuarios(), listarRoles()])
      .then(([u, r]) => { setUsuarios(u); setRoles(r); })
      .catch(() => setError('Error al cargar los datos.'))
      .finally(() => setCargando(false));
  };

  useEffect(() => { cargar(); }, []);

  const handleCrear = async e => {
    e.preventDefault();
    setError('');
    setExito('');
    setCreando(true);
    try {
      const rolSeleccionado = roles.find(r => r.id === form.rolId);
      await crearUsuario({
        id: form.id,
        mail: form.mail,
        pass: form.pass,
        estado: form.estado,
        rol: rolSeleccionado || null,
        persona: null,
        fechaRegistro: new Date().toISOString(),
      });
      setExito('Usuario creado correctamente.');
      setMostrarForm(false);
      setForm({ id: '', mail: '', pass: '', estado: 'ACTIVO', rolId: '' });
      cargar();
    } catch {
      setError('Error al crear el usuario. Verifica que el ID y correo no estén en uso.');
    } finally {
      setCreando(false);
    }
  };

  return (
    <Layout>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ fontFamily: "'DM Serif Display', serif", fontSize: 26, color: 'var(--azul)' }}>
          Gestión de usuarios
        </h2>
        <button className="btn btn-primario" onClick={() => setMostrarForm(!mostrarForm)}>
          {mostrarForm ? 'Cancelar' : '+ Nuevo usuario'}
        </button>
      </div>

      {error && <div className="alerta alerta-error">{error}</div>}
      {exito && <div className="alerta alerta-exito">{exito}</div>}

      {/* Formulario crear usuario */}
      {mostrarForm && (
        <div className="card">
          <h3 className="card-titulo">Crear nuevo usuario</h3>
          <form onSubmit={handleCrear}>
            <div className="grid-2">
              <div className="form-group">
                <label>ID de usuario (único)</label>
                <input
                  type="text"
                  className="form-control"
                  value={form.id}
                  onChange={e => setForm({ ...form, id: e.target.value })}
                  placeholder="Ej: USR001"
                  required
                />
              </div>
              <div className="form-group">
                <label>Correo electrónico</label>
                <input
                  type="email"
                  className="form-control"
                  value={form.mail}
                  onChange={e => setForm({ ...form, mail: e.target.value })}
                  placeholder="usuario@correo.cl"
                  required
                />
              </div>
              <div className="form-group">
                <label>Contraseña temporal</label>
                <input
                  type="password"
                  className="form-control"
                  value={form.pass}
                  onChange={e => setForm({ ...form, pass: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Rol</label>
                <select
                  className="form-control"
                  value={form.rolId}
                  onChange={e => setForm({ ...form, rolId: e.target.value })}
                  required
                >
                  <option value="">Selecciona un rol...</option>
                  {roles.map(r => (
                    <option key={r.id} value={r.id}>{r.nombre} ({r.tag})</option>
                  ))}
                </select>
              </div>
            </div>
            <button type="submit" className="btn btn-primario" disabled={creando}>
              {creando ? 'Creando...' : 'Crear usuario'}
            </button>
          </form>
        </div>
      )}

      {/* Tabla de usuarios */}
      {cargando ? (
        <div className="spinner">Cargando usuarios...</div>
      ) : (
        <div className="card">
          <table className="tabla">
            <thead>
              <tr>
                <th>ID</th>
                <th>Correo</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Registro</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map(u => (
                <tr key={u.id}>
                  <td style={{ fontFamily: 'monospace', fontSize: 13 }}>{u.id}</td>
                  <td>{u.mail}</td>
                  <td>
                    {u.rol ? (
                      <span style={{ background: 'var(--azul-suave)', color: 'var(--azul)', borderRadius: 6, padding: '2px 8px', fontSize: 12, fontWeight: 600 }}>
                        {u.rol.tag}
                      </span>
                    ) : '—'}
                  </td>
                  <td>
                    <span style={{
                      background: u.estado === 'ACTIVO' ? 'var(--verde-suave)' : 'var(--rojo-suave)',
                      color: u.estado === 'ACTIVO' ? 'var(--verde)' : 'var(--rojo)',
                      borderRadius: 6,
                      padding: '2px 8px',
                      fontSize: 12,
                      fontWeight: 600,
                    }}>
                      {u.estado}
                    </span>
                  </td>
                  <td style={{ fontSize: 13, color: 'var(--texto-suave)' }}>
                    {u.fechaRegistro ? new Date(u.fechaRegistro).toLocaleDateString('es-CL') : '—'}
                  </td>
                </tr>
              ))}
              {usuarios.length === 0 && (
                <tr>
                  <td colSpan={5} style={{ textAlign: 'center', padding: 32, color: 'var(--texto-suave)' }}>
                    No hay usuarios registrados.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
