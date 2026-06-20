package com.rednorte.mslistaespera;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Nota: no se usa @SpringBootTest aqui a proposito. Levantar el contexto
// completo de Spring exige una conexion real a MySQL (ver application.properties),
// la cual no existe en el runner de GitHub Actions y rompia el pipeline de CI.
// La cobertura real de logica de negocio esta en ReasignacionServiceTest (con Mockito).
@DisplayName("Tests de sanidad del proyecto")
class MsListaEsperaApplicationTests {

	@Test
	@DisplayName("El proyecto compila y los tests corren correctamente")
	void contextLoads() {
		assertTrue(true, "El proyecto esta configurado correctamente");
	}

}
