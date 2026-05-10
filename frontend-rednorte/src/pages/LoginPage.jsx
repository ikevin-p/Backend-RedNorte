import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login } from '../services/api';

export default function LoginPage() {
  const [mail, setMail] = useState('');
  const [pass, setPass] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);
  const { iniciarSesion } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setCargando(true);
    try {
      const usuario = await login(mail, pass);
      iniciarSesion(usuario);
      // Si es admin, va al panel admin; si no, al panel del paciente
      if (usuario?.rol?.tag === 'ADMIN') {
        navigate('/admin/consultas');
      } else {
        navigate('/mis-consultas');
      }
    } catch (err) {
      setError('Correo o contraseña incorrectos. Intenta de nuevo.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>RedNorte</h1>
        <p>Plataforma de gestión de listas de espera</p>

        {error && <div className="alerta alerta-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Correo electrónico</label>
            <input
              type="email"
              className="form-control"
              value={mail}
              onChange={e => setMail(e.target.value)}
              placeholder="tu@correo.cl"
              required
            />
          </div>
          <div className="form-group">
            <label>Contraseña</label>
            <input
              type="password"
              className="form-control"
              value={pass}
              onChange={e => setPass(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>
          <button
            type="submit"
            className="btn btn-primario"
            style={{ width: '100%', justifyContent: 'center', marginTop: 8 }}
            disabled={cargando}
          >
            {cargando ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>
      </div>
    </div>
  );
}
