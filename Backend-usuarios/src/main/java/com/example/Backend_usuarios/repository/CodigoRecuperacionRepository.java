package com.example.Backend_usuarios.repository;

import com.example.Backend_usuarios.model.CodigoRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {
    Optional<CodigoRecuperacion> findTopByMailAndCodigoOrderByCreadoDesc(String mail, String codigo);
}
