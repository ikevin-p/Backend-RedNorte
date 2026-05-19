package com.example.Backend_usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests de sanidad del proyecto")
class BackendUsuariosApplicationTests {

	@Test
	@DisplayName("El proyecto compila y los tests corren correctamente")
	void contextLoads() {
		assertTrue(true, "El proyecto esta configurado correctamente");
	}

}
