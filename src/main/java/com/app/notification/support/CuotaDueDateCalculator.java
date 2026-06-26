package com.app.notification.support;

import java.time.LocalDate;
import java.time.YearMonth;

import com.app.entity.Club;

public final class CuotaDueDateCalculator {

	private CuotaDueDateCalculator() {
	}

	public static LocalDate dueDateForMonth(Club club, int mes, int anio) {
		int diaBase = club != null && club.getDiaVencimientoCuota() != null ? club.getDiaVencimientoCuota() : 1;
		int max = YearMonth.of(anio, mes).lengthOfMonth();
		int day = Math.min(Math.max(diaBase, 1), max);
		return LocalDate.of(anio, mes, day);
	}
}
