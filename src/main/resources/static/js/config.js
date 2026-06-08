// Minimal config to avoid duplicating globals from js/scripts.js
// Define `API_URL` only if not already defined by other scripts.
if (typeof API_URL === 'undefined') {
    var API_URL = (location.hostname === 'localhost' || location.hostname === '127.0.0.1')
        ? 'http://localhost:8080/api'
        : '/api';
}

// Expose a small helper to navigate (non-intrusive)
function navegarPara(pagina) {
    window.location.href = pagina;
}