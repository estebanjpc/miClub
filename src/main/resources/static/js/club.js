function previewImage(event) {
        const file = event.target.files[0];
        const preview = document.getElementById('previewLogo');
        const current = document.getElementById('currentLogo');
        const removeBtn = document.getElementById('removeLogoBtn');
        const eliminarLogo = document.getElementById('eliminarLogo');

        if (file) {
            const reader = new FileReader();
            reader.onload = function() {
                preview.src = reader.result;
                preview.style.display = 'block';
                if (current) current.style.display = 'none';
                if (removeBtn) removeBtn.style.display = 'none';
                eliminarLogo.value = "false"; // No eliminar, solo reemplazar
            };
            reader.readAsDataURL(file);
        }
    }

    // Eliminar el logo actual
    function removeCurrentLogo() {
        const current = document.getElementById('currentLogo');
        const removeBtn = document.getElementById('removeLogoBtn');
        const preview = document.getElementById('previewLogo');
        const eliminarLogo = document.getElementById('eliminarLogo');

        if (current) current.style.display = 'none';
        if (removeBtn) removeBtn.style.display = 'none';
        if (preview) preview.style.display = 'none';
        eliminarLogo.value = "true"; // Marcar para eliminar en el backend
    }