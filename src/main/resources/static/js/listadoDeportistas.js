function eliminarUsuario(id) {
	const url = `${location.origin}/eliminarUsuario/${id}`;
	Swal.fire({
		title: '<h4 class="fw-bold text-danger"><i class="fas fa-trash-alt"></i> Eliminar apoderado</h4>',
		html: '<p class="mb-2">¿Estás seguro de eliminar este apoderado? Se eliminarán también sus deportistas y datos asociados. Esta acción no se puede deshacer.</p>',
		icon: 'warning',
		iconColor: '#ffc107',
		showCancelButton: true,
		confirmButtonText: '<i class="fas fa-check"></i> Sí, eliminar',
		cancelButtonText: '<i class="fas fa-times"></i> Cancelar',
		confirmButtonColor: '#dc3545',
		cancelButtonColor: '#6c757d',
		reverseButtons: true,
		background: '#f8f9fa',
		backdrop: 'rgba(0,0,0,0.5)',
		showClass: { popup: 'animate__animated animate__fadeInDown' },
		hideClass: { popup: 'animate__animated animate__fadeOutUp' }
	}).then((result) => {
		if (result.isConfirmed) {
			if (typeof showDiv === 'function') showDiv(true);
			window.location.href = url;
		}
	});
}

document.addEventListener('DOMContentLoaded', function () {
	const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
	tooltipTriggerList.forEach(function (el) {
		if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
			new bootstrap.Tooltip(el);
		}
	});
});
