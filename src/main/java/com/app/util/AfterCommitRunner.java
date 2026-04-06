package com.app.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Ejecuta código tras el commit de la transacción activa (si existe);
 * si no hay transacción, ejecuta de inmediato. Útil para no enviar correos
 * si el commit falla y para que el hilo de correo vea datos ya persistidos.
 */
public final class AfterCommitRunner {

	private AfterCommitRunner() {
	}

	public static void run(Runnable action) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					action.run();
				}
			});
		} else {
			action.run();
		}
	}
}
