import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { listarTodasConsultas, listarUsuarios } from '../services/api';

function StatCard({ valor, label, color }) {
  return (
    <div style={{
      background: 'var(--blanco)',
      borderRadius: 'var(--radio)',
      boxShadow: 'var(--sombra)',
      padding: '24px',
      textAlign: 'center',
      borderTop: `4px solid ${color}`,
    }}>
      <div style={{ fontSize: 38, fontWeight: 600, color, fontFamily: "'DM Serif Display', serif" }}>
        {valor}
      </div>
      <div style={{ fontSize: 13, color: 'var(--texto-suave)', marginTop: 6, fontWeight: 500 }}>
        {label}
      </div>
    </div>
  );
}

export default function AdminDashboardPage() {
  const [consultas, setConsultas] = useState([]);
  const [usuarios, setUsuarios] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    Promise.all([listarTodasConsultas(), listarUsuarios()])
      .then(([c, u]) => { setConsultas(c); setUsuarios(u); })
      .finally(() => setCargando(false));
  }, []);

  const contarEstado = (estado) => consultas.filter(c => c.estado === estado).length;

  const especialidades = consultas.reduce((acc, c) => {
    acc[c.especialidad] = (acc[c.especialidad] || 0) + 1;
    return acc;
  }, {});

  const topEspecialidades = Object.entries(especialidades)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5);

  const maxEsp = topEspecialidades[0]?.[1] || 1;

  const recientes = [...consultas]
    .sort((a, b) => new Date(b.fechaCreacion) - new Date(a.fechaCreacion))
    .slice(0, 5);

  return (
    <Layout>
      <h2 style={{ fontFamily: "'DM Serif Display', serif", fontSize: 28, color: 'var(--azul)', marginBottom: 6 }}>
        Panel de control
      </h2>
      <p style={{ color: 'var(--texto-suave)', fontSize: 14, marginBottom: 28 }}>
        Resumen general del sistema RedNorte
      </p>

      {cargando ? (
        <div className="spinner">Cargando estadísticas...</div>
      ) : (
        <>
          {/* Tarjetas de estadísticas */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: 16, marginBottom: 28 }}>
            <StatCard valor={consultas.length}       label="Total consultas"    color="#2d7dd2" />
            <StatCard valor={contarEstado('PENDIENTE')}  label="Pendientes"     color="#d97706" />
            <StatCard valor={contarEstado('AGENDADA')}   label="Agendadas"      color="#1a4f8a" />
            <StatCard valor={contarEstado('REASIGNADA')} label="Reasignadas"    color="#16a37f" />
            <StatCard valor={contarEstado('ATENDIDA')}   label="Atendidas"      color="#059669" />
            <StatCard valor={usuarios.filter(u => u.rol?.tag === 'PACIENTE').length} label="Pacientes registrados" color="#7c3aed" />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 20 }}>
            {/* Top especialidades */}
            <div className="card">
              <h3 className="card-titulo" style={{ fontSize: 16 }}>Especialidades más solicitadas</h3>
              {topEspecialidades.length === 0 ? (
                <p style={{ color: 'var(--texto-suave)', fontSize: 14 }}>Sin datos aún.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  {topEspecialidades.map(([esp, count]) => (
                    <div key={esp}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
                        <span style={{ textTransform: 'capitalize', fontWeight: 500 }}>{esp}</span>
                        <span style={{ color: 'var(--texto-suave)' }}>{count} consulta{count !== 1 ? 's' : ''}</span>
                      </div>
                      <div style={{ background: 'var(--gris-bg)', borderRadius: 4, height: 8, overflow: 'hidden' }}>
                        <div style={{
                          height: '100%',
                          width: `${(count / maxEsp) * 100}%`,
                          background: 'var(--azul-claro)',
                          borderRadius: 4,
                          transition: 'width 0.4s ease',
                        }} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Distribución por estado */}
            <div className="card">
              <h3 className="card-titulo" style={{ fontSize: 16 }}>Estado de consultas</h3>
              {[
                { estado: 'PENDIENTE',  color: '#d97706', bg: 'var(--amarillo-suave)' },
                { estado: 'AGENDADA',   color: '#1a4f8a', bg: 'var(--azul-suave)' },
                { estado: 'REASIGNADA', color: '#16a37f', bg: 'var(--verde-suave)' },
                { estado: 'CANCELADA',  color: '#d63031', bg: 'var(--rojo-suave)' },
                { estado: 'ATENDIDA',   color: '#059669', bg: '#f0fdf4' },
              ].map(({ estado, color, bg }) => {
                const n = contarEstado(estado);
                const pct = consultas.length ? Math.round((n / consultas.length) * 100) : 0;
                return (
                  <div key={estado} style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
                    <div style={{ background: bg, color, borderRadius: 6, padding: '3px 10px', fontSize: 12, fontWeight: 600, minWidth: 96, textAlign: 'center' }}>
                      {estado}
                    </div>
                    <div style={{ flex: 1, background: 'var(--gris-bg)', borderRadius: 4, height: 8, overflow: 'hidden' }}>
                      <div style={{ height: '100%', width: `${pct}%`, background: color, borderRadius: 4, transition: 'width 0.4s ease' }} />
                    </div>
                    <div style={{ fontSize: 13, color: 'var(--texto-suave)', minWidth: 28, textAlign: 'right' }}>{n}</div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Consultas recientes */}
          <div className="card">
            <h3 className="card-titulo" style={{ fontSize: 16 }}>Últimas consultas ingresadas</h3>
            {recientes.length === 0 ? (
              <p style={{ color: 'var(--texto-suave)', fontSize: 14 }}>Sin consultas aún.</p>
            ) : (
              <table className="tabla">
                <thead>
                  <tr>
                    <th>N°</th>
                    <th>Paciente</th>
                    <th>Especialidad</th>
                    <th>Estado</th>
                    <th>Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {recientes.map(c => (
                    <tr key={c.id}>
                      <td style={{ fontWeight: 500 }}>#{c.id}</td>
                      <td>{c.nombrePaciente || '—'}</td>
                      <td style={{ textTransform: 'capitalize', fontSize: 13 }}>{c.especialidad}</td>
                      <td>
                        <span className={`badge badge-${c.estado.toLowerCase()}`}>{c.estado}</span>
                      </td>
                      <td style={{ fontSize: 13, color: 'var(--texto-suave)' }}>
                        {new Date(c.fechaCreacion).toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric' })}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </Layout>
  );
}
