package com.rednorte.msficha.service;

import com.rednorte.msficha.model.FichaMedica;
import com.rednorte.msficha.repository.FichaMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FichaMedicaService {

    @Autowired
    private FichaMedicaRepository repo;

    public FichaMedica obtenerPorUsuario(String usuarioId) {
        return repo.findByUsuarioId(usuarioId).orElse(null);
    }

    public FichaMedica guardar(String usuarioId, FichaMedica datos) {
        Optional<FichaMedica> existente = repo.findByUsuarioId(usuarioId);
        FichaMedica ficha = existente.orElse(new FichaMedica());
        ficha.setUsuarioId(usuarioId);
        ficha.setEstatura(datos.getEstatura());
        ficha.setPeso(datos.getPeso());
        ficha.setGrupoSanguineo(datos.getGrupoSanguineo());
        ficha.setPresionArterial(datos.getPresionArterial());
        ficha.setFrecuenciaCardiaca(datos.getFrecuenciaCardiaca());
        ficha.setGlucosa(datos.getGlucosa());
        ficha.setTelefono(datos.getTelefono());
        ficha.setDireccion(datos.getDireccion());
        ficha.setAlergias(datos.getAlergias());
        ficha.setCondicionesCronicas(datos.getCondicionesCronicas());
        ficha.setMedicamentosActuales(datos.getMedicamentosActuales());
        ficha.setCirugiasPrevias(datos.getCirugiasPrevias());
        ficha.setAntecedentesFamiliares(datos.getAntecedentesFamiliares());
        ficha.setHabitoTabaco(datos.getHabitoTabaco());
        ficha.setHabitoAlcohol(datos.getHabitoAlcohol());
        ficha.setEmergenciaNombre(datos.getEmergenciaNombre());
        ficha.setEmergenciaTelefono(datos.getEmergenciaTelefono());
        ficha.setEmergenciaRelacion(datos.getEmergenciaRelacion());
        return repo.save(ficha);
    }
}
