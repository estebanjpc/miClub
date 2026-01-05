document.addEventListener("DOMContentLoaded", function () {
		const form = document.getElementById("filtrosForm");
		const selects = form.querySelectorAll("select");

		selects.forEach(select => {
			select.addEventListener("change", () => {
				form.submit();
			});
		});
	});