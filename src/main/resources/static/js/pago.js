// Funciones existentes (calcular total) mantenidas
document.querySelectorAll('.chkMes').forEach(chk => {
	chk.addEventListener('change', calcularTotal);
});

function calcularTotal() {
	let total = 0;
	document.querySelectorAll('.chkMes:checked').forEach(chk => {
		total += parseInt(chk.dataset.valor) || 0;
	});
	document.getElementById("totalPagar").innerText = total;
}

// ----------------- manejo de tarjetas sin radios -----------------
function initMetodoPagoCards() {
	const cards = Array.from(document.querySelectorAll('.metodo-pago-card'));
	const hiddenInput = document.getElementById('medioPagoHidden');
	if (!hiddenInput) return;

	// click en tarjeta: marca visualmente y guarda valor en hidden
	cards.forEach(card => {
		// hacerla "clickeable" por teclado
		card.setAttribute('tabindex', '0');

		card.addEventListener('click', () => {
			selectCard(card, hiddenInput, cards);
		});

		card.addEventListener('keydown', (e) => {
			if (e.key === 'Enter' || e.key === ' ') {
				e.preventDefault();
				selectCard(card, hiddenInput, cards);
			}
		});
	});

	// Si el hidden ya tiene valor (ej: recarga), sincronizar UI
	if (hiddenInput.value) {
		const pre = cards.find(c => c.dataset.value === hiddenInput.value);
		if (pre) pre.classList.add('selected');
	}
}

function selectCard(card, hiddenInput, allCards) {
	// remover selected de todas
	allCards.forEach(c => c.classList.remove('selected'));

	// marcar la actual
	card.classList.add('selected');

	// guardar valor en hidden (esto es lo que se enviará en el form)
	const val = card.dataset.value || '';
	hiddenInput.value = val;

	// opcional: disparar evento change en el hidden (por si lo escuchas)
	hiddenInput.dispatchEvent(new Event('change', { bubbles: true }));
}

// Validación extra antes de enviar (por si usas required en hidden)
const form = document.getElementById('formPago');
if (form) {
	form.addEventListener('submit', (e) => {

		const hiddenInput = document.getElementById('medioPagoHidden');
		const seleccionados = document.querySelectorAll('.chkMes:checked');

		// Validar método de pago
		if (!hiddenInput || !hiddenInput.value) {
			e.preventDefault();
			mostrarAlerta("error;Error;Selecciona un método de pago antes de continuar");

			const firstCard = document.querySelector('.metodo-pago-card');
			if (firstCard) firstCard.focus();
			return;
		}

		// Validar al menos un mes seleccionado
		if (seleccionados.length === 0) {
			e.preventDefault();
			mostrarAlerta("error;Error;Debes seleccionar al menos un mes a pagar");

			// Hacer foco en el primer checkbox visible
			const firstChk = document.querySelector('.chkMes');
			if (firstChk) firstChk.focus();
			return;
		}

	});
}

// inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
	document.addEventListener('DOMContentLoaded', initMetodoPagoCards);
} else {
	initMetodoPagoCards();
}
