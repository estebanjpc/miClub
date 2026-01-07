document.querySelectorAll(".nav-link").forEach((link) => {
	link.classList.remove('active');
    if (link.href === window.location.href) {
        link.classList.add("active");
        link.setAttribute("aria-current", "page");
    }
});

function showDiv(flag){
	if(flag) document.getElementById("loader").style.display = 'block';
	else document.getElementById("loader").style.display = 'none';	
}

function mostrarAlerta(msj) {
    if (!msj) return;

    const aux = msj.split(';'); // [tipo, titulo, mensaje]
    if (aux.length < 3) return;

    let iconColor = '#0d6efd'; // azul por defecto
    switch(aux[0]) {
        case 'success': iconColor = '#198754'; break;
        case 'error': iconColor = '#dc3545'; break;
        case 'warning': iconColor = '#ffc107'; break;
        case 'info': iconColor = '#0dcaf0'; break;
    }

    Swal.fire({
        title: `<h4 class="fw-bold" style="color:${iconColor}">${aux[1]}</h4>`,
        html: `<p class="mb-2">${aux[2]}</p>`,
        icon: aux[0],
        iconColor: iconColor,
        confirmButtonText: '<i class="fas fa-check"></i> Aceptar',
        confirmButtonColor: iconColor,
        background: '#f8f9fa',
        backdrop: 'rgba(0,0,0,0.5)',
        showClass: { popup: 'animate__animated animate__fadeInDown' },
        hideClass: { popup: 'animate__animated animate__fadeOutUp' }
    });
}