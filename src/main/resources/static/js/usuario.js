let index = document.querySelectorAll(".deportista-item").length;

// Agregar nuevo deportista
document.getElementById("btnAgregarDeportista").addEventListener("click", function() {
	const container = document.getElementById("deportistasContainer");
	const template = document.querySelector(".deportista-item").cloneNode(true);

	// Limpiar valores
	template.querySelectorAll("input, select").forEach(el => el.value = "");

	// Actualizar nombres para Spring
	template.querySelectorAll("[th\\:field], [name]").forEach(el => {
		if (el.hasAttribute("th:field")) {
			let campo = el.getAttribute("th:field").replace(/\[\d+\]/, `[${index}]`);
			el.setAttribute("name", campo.replace("*{", "").replace("}", ""));
		} else {
			el.name = el.name.replace(/\d+/, index);
		}
	});


	// Añadir evento para eliminar
	template.querySelector(".remove-deportista").addEventListener("click", () => template.remove());

	container.appendChild(template);
	index++;
});

// Eliminar deportista
document.querySelectorAll(".remove-deportista").forEach(btn => {
	btn.addEventListener("click", function() {
		this.closest(".deportista-item").remove();
	});
});

// Copiar datos del apoderado al primer deportista
document.getElementById("chkMismo").addEventListener("change", function() {
	const checked = this.checked;
	const d = document.querySelector(".deportista-item");
	if (!d) return;
	if (checked) {
		d.querySelector("[name='deportistas[0].nombre']").value = document.querySelector("[name='nombre']").value;
		d.querySelector("[name='deportistas[0].apellido']").value = document.querySelector("[name='apellido']").value;
		d.querySelector("[name='deportistas[0].rut']").value = document.querySelector("[name='rut']").value;
	} else {
		d.querySelector("[name='deportistas[0].nombre']").value = "";
		d.querySelector("[name='deportistas[0].apellido']").value = "";
		d.querySelector("[name='deportistas[0].rut']").value = "";
	}
});


// QUITA EL COLOR DE ERROR CUANDO ESCRIBEN EN EL INPUT
document.addEventListener("DOMContentLoaded", function() {
	// Selecciona todos los inputs y selects dentro del formulario
	const form = document.getElementById("idForm");
	const fields = form.querySelectorAll("input, select, textarea");

	fields.forEach(field => {
		// Al escribir o cambiar el valor
		field.addEventListener("input", function() {
			if (field.classList.contains("is-invalid")) {
				field.classList.remove("is-invalid");
				// Opcional: también puedes ocultar el mensaje de error
				const feedback = field.parentElement.querySelector(".invalid-feedback");
				if (feedback) feedback.style.display = "none";
			}
		});
		field.addEventListener("change", function() { // Para selects
			if (field.classList.contains("is-invalid")) {
				field.classList.remove("is-invalid");
				const feedback = field.parentElement.querySelector(".invalid-feedback");
				if (feedback) feedback.style.display = "none";
			}
		});
	});
});