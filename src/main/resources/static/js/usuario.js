document.addEventListener("DOMContentLoaded", function() {
	const form = document.getElementById("idForm");
	const modoStaff = form && form.getAttribute("data-es-staff") === "true";

	if (!modoStaff) {
		let index = document.querySelectorAll(".deportista-item").length;

		const btnAgregar = document.getElementById("btnAgregarDeportista");
		if (btnAgregar) {
			btnAgregar.addEventListener("click", function() {
				const container = document.getElementById("deportistasContainer");
				const first = document.querySelector(".deportista-item");
				if (!container || !first) {
					return;
				}
				const template = first.cloneNode(true);

				template.querySelectorAll("input, select").forEach(el => el.value = "");

				template.querySelectorAll("[th\\:field], [name]").forEach(el => {
					if (el.hasAttribute("th:field")) {
						let campo = el.getAttribute("th:field").replace(/\[\d+\]/, `[${index}]`);
						el.setAttribute("name", campo.replace("*{", "").replace("}", ""));
					} else {
						el.name = el.name.replace(/\d+/, index);
					}
				});

				template.querySelector(".remove-deportista").addEventListener("click", () => template.remove());

				container.appendChild(template);
				index++;
			});
		}

		document.querySelectorAll(".remove-deportista").forEach(btn => {
			btn.addEventListener("click", function() {
				this.closest(".deportista-item").remove();
			});
		});

		const chkMismo = document.getElementById("chkMismo");
		if (chkMismo) {
			chkMismo.addEventListener("change", function() {
				const checked = this.checked;
				const d = document.querySelector(".deportista-item");
				if (!d) return;
				const n0 = d.querySelector("[name='deportistas[0].nombre']");
				const a0 = d.querySelector("[name='deportistas[0].apellido']");
				const r0 = d.querySelector("[name='deportistas[0].rut']");
				const nombre = document.querySelector("[name='nombre']");
				const apellido = document.querySelector("[name='apellido']");
				const rut = document.querySelector("[name='rut']");
				if (!n0 || !nombre) return;
				if (checked) {
					n0.value = nombre.value;
					if (a0 && apellido) a0.value = apellido.value;
					if (r0 && rut) r0.value = rut.value;
				} else {
					n0.value = "";
					if (a0) a0.value = "";
					if (r0) r0.value = "";
				}
			});
		}
	}

	const formEl = document.getElementById("idForm");
	if (!formEl) return;

	const fields = formEl.querySelectorAll("input, select, textarea");
	fields.forEach(field => {
		field.addEventListener("input", function() {
			if (field.classList.contains("is-invalid")) {
				field.classList.remove("is-invalid");
				const feedback = field.parentElement.querySelector(".invalid-feedback");
				if (feedback) feedback.style.display = "none";
			}
		});
		field.addEventListener("change", function() {
			if (field.classList.contains("is-invalid")) {
				field.classList.remove("is-invalid");
				const feedback = field.parentElement.querySelector(".invalid-feedback");
				if (feedback) feedback.style.display = "none";
			}
		});
	});
});
