package com.app.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class UtilTest {

	@Test
	void generatePassword_respetaLongitud() {
		assertThat(Util.generatePassword(8)).hasSize(8);
		assertThat(Util.generatePassword(20)).hasSize(20);
	}

	@RepeatedTest(5)
	void generatePassword_soloCaracteresPermitidos() {
		assertThat(Util.generatePassword(32)).matches("[A-Za-z0-9]{32}");
	}

	@Test
	void toSlug_normalizaTexto() {
		assertThat(Util.toSlug("Hola Mundo")).isEqualTo("hola-mundo");
		assertThat(Util.toSlug("  Foo  Bar  ")).containsPattern("foo");
	}

}
