document.addEventListener("DOMContentLoaded", function () {
	const form = document.getElementById("filtrosForm");
	if (form) {
		const selects = form.querySelectorAll("select");
		selects.forEach(select => {
			select.addEventListener("change", () => {
				form.submit();
			});
		});
	}

	const hash = window.location.hash;
	if (hash && hash.length > 1) {
		const el = document.querySelector(hash);
		if (el) {
			requestAnimationFrame(() => {
				el.scrollIntoView({ behavior: "smooth", block: "start" });
			});
		}
	}
});
