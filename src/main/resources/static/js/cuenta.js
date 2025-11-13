document.addEventListener("DOMContentLoaded", function() {
    // --- Scroll automático a primer error ---
    const firstInvalid = document.querySelector(".is-invalid");
    if (firstInvalid) {
        firstInvalid.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    // --- Quitar color rojo (is-invalid) al corregir ---
    const invalidFields = document.querySelectorAll(".is-invalid");

    invalidFields.forEach(field => {
        // Si es input o textarea
        field.addEventListener("input", function() {
            if (field.value.trim() !== "") {
                field.classList.remove("is-invalid");
            }
        });

        // Si es select
        field.addEventListener("change", function() {
            if (field.value.trim() !== "") {
                field.classList.remove("is-invalid");
            }
        });
    });
});



function eliminarCuenta(id) {
    const url = `${location.origin}/cuentas/eliminar/${id}`;
    Swal.fire({
        title: '<h4 class="fw-bold text-danger"><i class="fas fa-trash-alt"></i> Eliminar Cuenta Bancaria</h4>',
        html: `<p class="mb-2">¿Estás seguro que deseas eliminar la Cuenta Bancaria?</p>`,
        icon: 'warning',
        iconColor: '#ffc107',
        showCancelButton: true,
        confirmButtonText: '<i class="fas fa-check"></i> Sí, eliminar',
        cancelButtonText: '<i class="fas fa-times"></i> Cancelar',
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#6c757d',
        reverseButtons: true,
        background: '#f8f9fa',
        backdrop: `rgba(0,0,0,0.5)`,
        showClass: {
            popup: 'animate__animated animate__fadeInDown'
        },
        hideClass: {
            popup: 'animate__animated animate__fadeOutUp'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            showDiv(true);
            window.location = url;
        }
    });
}