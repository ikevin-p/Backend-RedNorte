import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import EstadoBadge from '../components/EstadoBadge';
import { listarTodasConsultas, actualizarConsultaAdmin, eliminarConsulta } from '../services/api';

const ESTADOS = ['PENDIENTE', 'AGENDADA', 'CANCELADA', 'REASIGNADA', 'ATENDIDA'];

export default function AdminConsultasPage() {
  const [consultas, setConsultas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [editando, setEditando] = useState(null); // ID de la consulta en edición
  const [formAdmin, setFormAdmin] = useState({ estado: '', fechaCita: '', notasAdmin: '' });
  const [guardando, setGuardando] = useState(false);
  const [filtroEstado, setFiltroEstado] = useState('');
  const [filtroEspecialidad, setFiltroEspecialidad] = useState('');

  const cargar = () => {
    setCargando(true);
    listarTodasConsultas()
      .then(setConsultas)
      .catch(() => setError('Error al cargar las consultas.'))
      .finally(() => setCargando(false));
  };

  useEffect(() => { cargar(); }, []);

  const abrirEditor = (c) => {
    setEditando(c.id);
    setFormAdmin({
      estado: c.estado,
      fechaCita: c.fechaCita ? c.fechaCita.slice(0, 16) : '',
      notasAdmin: c.notasAdmin || '',
    });
  };

  const guardarCambios = async () => {
    setGuardando(true);
    try {
      const dto = {
        estado: formAdmin.estado,
        fechaCita: formAdmin.fechaCita ? formAdmin.fechaCita + ':00' : null,
        notasAdmin: formAdmin.notasAdmin,
      };
      await actualizarConsultaAdmin(editando, dto);
      setEditando(null);
      cargar();
    } catch {
      setError('Error al guardar los cambios.');
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async (id) => {
    if (!window.confirm('¿Eliminar esta consulta? Esta acción no se puede deshacer.')) return;
    try {
      await eliminarConsulta(id);
      cargar();
    } catch {
      setError('Error al eliminar la consulta.');
    }
  };

  const consultasFiltradas = consultas.filter(c => {
    if (filtroEstado && c.estado !== filtroEstado) return false;
    if (filtroEspecialidad && !c.especialidad.includes(filtroEspecialidad.toLowerCase())) return false;
    return true;
  });

  return (
    <Layout>
      <h2 style={{ fontFamily: "'DM Serif Display', serif", fontSize: 26, color: 'var(--azul)', marginBottom: 24 }}>
        Gestión de consultas
      </h2>

      {error && <div className="alerta alerta-error">{error}</div>}

      {/* Filtros */}
      <div className="card" style={{ padding: '16px 24px' }}>
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <div className="form-group" style={{ margin: 0, flex: 1, minWidth: 140 }}>
            <label>Filtrar por estado</label>
            <select className="form-control" value={filtroEstado} onChange={e => setFiltroEstado(e.target.value)}>
              <option value="">Todos</option>
              {ESTADOS.map(e => <option key={e} value={e}>{e}</option>)}
            </select>
          </div>
          <div className="form-group" style={{ margin: 0, flex: 1, minWidth: 160 }}>
            <label>Filtrar por especialidad</label>
            <input
              type="text"
              className="form-control"
              value={filtroEspecialidad}
              onChange={e => setFiltroEspecialidad(e.target.value)}
              placeholder="Ej: cardiologia"
            />
          </div>
          <button className="btn btn-secundario btn-sm" onClick={() => { setFiltroEstado(''); setFiltroEspecialidad(''); }}>
            Limpiar
          </button>
        </div>
      </div>

      {cargando ? (
        <div className="spinner">Cargando consultas...</div>
      ) : (
        <div className="card">
          <div style={{ marginBottom: 12, color: 'var(--texto-suave)', fontSize: 13 }}>
            {consultasFiltradas.length} consulta{consultasFiltradas.length !== 1 ? 's' : ''} encontrada{consultasFiltradas.length !== 1 ? 's' : ''}
          </div>
          <table className="tabla">
            <thead>
              <tr>
                <th>N°</th>
                <th>Paciente</th>
                <th>Especialidad</th>
                <th>Síntomas</th>
                <th>Estado</th>
                <th>Fecha cita</th>
                <th>Notas admin</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {consultasFiltradas.map(c => (
                <React.Fragment key={c.id}>
                  <tr>
                    <td style={{ fontWeight: 500 }}>#{c.id}</td>
                    <td>
                      <div style={{ fontWeight: 500 }}>{c.nombrePaciente || '—'}</div>
                      <div style={{ fontSize: 12, color: 'var(--texto-suave)' }}>ID: {c.usuarioId}</div>
                    </td>
                    <td style={{ textTransform: 'capitalize' }}>{c.especialidad}</td>
                    <td style={{ maxWidth: 160, fontSize: 13, color: 'var(--texto-suave)' }}>
                      {c.sintomas ? c.sintomas.slice(0, 60) + (c.sintomas.length > 60 ? '...' : '') : '—'}
                    </td>
                    <td><EstadoBadge estado={c.estado} /></td>
                    <td style={{ fontSize: 13, whiteSpace: 'nowrap' }}>
                      {c.fechaCita
                        ? new Date(c.fechaCita).toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric' })
                        : '—'}
                    </td>
                    <td style={{ fontSize: 13, color: 'var(--texto-suave)', maxWidth: 140 }}>
                      {c.notasAdmin || '—'}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-secundario btn-sm" onClick={() => abrirEditor(c)}>
                          Editar
                        </button>
                        <button className="btn btn-peligro btn-sm" onClick={() => handleEliminar(c.id)}>
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>

                  {/* Panel de edición inline */}
                  {editando === c.id && (
                    <tr>
                      <td colSpan={8} style={{ background: 'var(--azul-suave)', padding: '20px 24px' }}>
                        <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 12, color: 'var(--azul)' }}>
                          Editando consulta #{c.id}
                        </div>
                        <div className="grid-2" style={{ gap: 12, maxWidth: 600 }}>
                          <div className="form-group" style={{ margin: 0 }}>
                            <label>Estado</label>
                            <select
                              className="form-control"
                              value={formAdmin.estado}
                              onChange={e => setFormAdmin({ ...formAdmin, estado: e.target.value })}
                            >
                              {ESTADOS.map(e => <option key={e} value={e}>{e}</option>)}
                            </select>
                          </div>
                          <div className="form-group" style={{ margin: 0 }}>
                            <label>Fecha de cita</label>
                            <input
                              type="datetime-local"
                              className="form-control"
                              value={formAdmin.fechaCita}
                              onChange={e => setFormAdmin({ ...formAdmin, fechaCita: e.target.value })}
                            />
                          </div>
                          <div className="form-group" style={{ margin: 0, gridColumn: '1 / -1' }}>
                            <label>Notas del administrador</label>
                            <textarea
                              className="form-control"
                              style={{ minHeight: 60 }}
                              value={formAdmin.notasAdmin}
                              onChange={e => setFormAdmin({ ...formAdmin, notasAdmin: e.target.value })}
                              placeholder="Notas internas sobre esta consulta..."
                            />
                          </div>
                        </div>
                        <div style={{ display: 'flex', gap: 10, marginTop: 14 }}>
                          <button className="btn btn-primario btn-sm" onClick={guardarCambios} disabled={guardando}>
                            {guardando ? 'Guardando...' : 'Guardar'}
                          </button>
                          <button className="btn btn-secundario btn-sm" onClick={() => setEditando(null)}>
                            Cancelar
                          </button>
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
              {consultasFiltradas.length === 0 && (
                <tr>
                  <td colSpan={8} style={{ textAlign: 'center', padding: 32, color: 'var(--texto-suave)' }}>
                    No hay consultas con esos filtros.
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
