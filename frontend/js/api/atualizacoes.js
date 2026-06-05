const API_BASE = 'http://localhost:8080/api';

function getAuthHeaders() {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

export async function listarAtualizacoes() {
    const resp = await fetch(`${API_BASE}/atualizacoes`, {
        headers: getAuthHeaders()
    });
    if (!resp.ok) return [];
    return resp.json();
}
