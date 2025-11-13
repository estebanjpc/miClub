function eliminarCategoria(id) {
    const url = `${location.origin}/categorias/eliminar/${id}`;
    Swal.fire({
        title: '<h4 class="fw-bold text-danger"><i class="fas fa-trash-alt"></i> Eliminar Categoria</h4>',
        html: `<p class="mb-2">¿Estás seguro que deseas eliminar la Categoria?</p>`,
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