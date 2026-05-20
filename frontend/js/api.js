// api.js — shared HTTP helper for the admin frontend

const API_BASE = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('jwt_token');
}

function setToken(token) {
    localStorage.setItem('jwt_token', token);
}

function clearToken() {
    localStorage.removeItem('jwt_token');
}

function isLoggedIn() {
    return !!getToken();
}

function redirectIfNotLoggedIn() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
    }
}

async function api(path, options = {}) {
    const url = API_BASE + path;
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    const token = getToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    const res = await fetch(url, { ...options, headers });

    if (res.status === 403) {
        clearToken();
        window.location.href = 'login.html';
        throw new Error('Unauthorized');
    }

    const json = await res.json();

    if (json.code && json.code !== 200) {
        throw new Error(json.message || 'Request failed');
    }

    return json.data;
}

async function loginApi(username, password) {
    const url = API_BASE + '/users/login';
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    const json = await res.json();
    if (json.code !== 200) {
        throw new Error(json.message || 'Login failed');
    }
    setToken(json.data.token);
    return json.data;
}

function logout() {
    clearToken();
    window.location.href = 'login.html';
}
