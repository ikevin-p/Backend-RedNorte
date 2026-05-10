import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import EstadoBadge from '../components/EstadoBadge';
import { useAuth } from '../context/AuthContext';
import { listarConsultasPorUsuario } from '../services/api';

export default function MisConsultasPage() {
  const { usuario } = useAuth();
  const [consultas, setConsultas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!usuario) return;
    listarConsultasPorUsuario(usuario.id)
      .then(setConsultas)
      .catch(() => setError('No se pudieron cargar tus consultas.'))
      .finally(() => setCargando(false));
  }, [usuario]);

  return (
    <Layout>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ fontFamily: "'DM Serif Display', serif", fontSize: 26, color: 'var(--azul)' }}>
          Mis consultas
        </h2>
        <Link to="/nueva-consulta" className="btn btn-primario">
          + Nueva consulta
        </Link>
      </div>

      {error && <div className="alerta alerta-error">{error}</div>}

      {cargando ? (
        <div className="spinner">Cargando tus consultas...</div>
      ) : consultas.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 48, color: 'var(--texto-suave)' }}>
          <p style={{ fontSize: 16, marginBottom: 16 }}>Aún no tienes consultas registradas.</p>
          <Link to="/nueva-consulta" className="btn btn-primario">Crear mi primera consulta</Link>
        </div>
      ) : (
        <div className="card">
          <table className="tabla">
            <thead>
              <tr>
                <th>N°</th>
                <th>Especialidad</th>
                <th>Síntomas</th>
                <th>Estado</th>
                <th>Fecha cita</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {consultas.map(c => (
                <tr key={c.id}>
                  <td style={{ fontWeight: 500 }}>#{c.id}</td>
                  <td style={{ textTransform: 'capitalize' }}>{c.especialidad}</td>
                  <td style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {c.sintomas || '—'}
                  </td>
                  <td><EstadoBadge estado={c.estado} /></td>
                  <td style={{ fontSize: 13, color: 'var(--texto-suave)' }}>
                    {c.fechaCita
                      ? new Date(c.fechaCita).toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
                      : '—'}
                  </td>
                  <td>
                    {(c.estado === 'PENDIENTE' || c.estado === 'AGENDADA') && (
                      <Link to={`/editar-consulta/${c.id}`} className="btn btn-secundario btn-sm">
                        Editar
                      </Link>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
