/* ============================================
   DIRECTHELP HUB - ADMIN PANEL JAVASCRIPT
   ============================================ */

const ADMIN_CONFIG = {
  apiBase: 'http://localhost:9090/api',
};

let allRequests = [];
let pendingDeleteId = null;

/* --- Theme (shared logic) --- */
function initTheme() {
  const saved = localStorage.getItem('theme') || 'dark';
  document.body.className = saved + '-mode';
  const icon = document.getElementById('themeIcon');
  if (icon) icon.className = saved === 'dark' ? 'fas fa-moon' : 'fas fa-sun';
}

document.getElementById('themeToggle').addEventListener('click', () => {
  const isDark = document.body.classList.contains('dark-mode');
  const next = isDark ? 'light' : 'dark';
  document.body.className = next + '-mode';
  localStorage.setItem('theme', next);
  const icon = document.getElementById('themeIcon');
  if (icon) icon.className = next === 'dark' ? 'fas fa-moon' : 'fas fa-sun';
});

/* --- Navbar scroll --- */
window.addEventListener('scroll', () => {
  document.getElementById('navbar').classList.toggle('scrolled', window.scrollY > 20);
});

/* --- Load Requests --- */
async function loadRequests() {
  const loading = document.getElementById('loadingState');
  const container = document.getElementById('tableContainer');
  const warn = document.getElementById('serverWarn');

  loading.style.display = 'block';
  container.style.display = 'none';
  warn.classList.remove('show');

  try {
    const res = await fetch(ADMIN_CONFIG.apiBase + '/requests', {
      signal: AbortSignal.timeout(4000),
    });

    if (!res.ok) throw new Error('Server error');
    const data = await res.json();
    allRequests = data.requests || [];
    renderRequests(allRequests);
    updateStats(allRequests);
  } catch (_) {
    warn.classList.add('show');
    allRequests = [];
    renderRequests([]);
    updateStats([]);
  }

  loading.style.display = 'none';
  container.style.display = 'block';
}

/* --- Render Table --- */
function renderRequests(requests) {
  const tbody = document.getElementById('tableBody');
  const noData = document.getElementById('noData');

  if (requests.length === 0) {
    tbody.innerHTML = '';
    noData.style.display = 'block';
    return;
  }

  noData.style.display = 'none';
  tbody.innerHTML = requests.map((r, i) => `
    <tr>
      <td>${i + 1}</td>
      <td><strong>${esc(r.name)}</strong></td>
      <td><a href="https://wa.me/${r.whatsapp.replace(/\D/g,'')}" target="_blank" style="color:var(--success)">${esc(r.whatsapp)}</a></td>
      <td>${r.email ? `<a href="mailto:${esc(r.email)}" style="color:var(--primary)">${esc(r.email)}</a>` : '<span style="color:var(--text2)">-</span>'}</td>
      <td><span class="badge ${badgeClass(r.category)}">${esc(r.category)}</span></td>
      <td class="msg-col">${esc(r.message)}</td>
      <td style="white-space:nowrap;color:var(--text2);font-size:.82rem">${formatDate(r.timestamp)}</td>
      <td><button class="btn-del" onclick="openDeleteModal('${esc(r.id)}')"><i class="fas fa-trash"></i> Delete</button></td>
    </tr>
  `).join('');
}

/* --- Stats --- */
function updateStats(requests) {
  const today = new Date().toISOString().slice(0, 10);
  document.getElementById('statTotal').textContent = requests.length;
  document.getElementById('statToday').textContent = requests.filter(r => r.timestamp && r.timestamp.startsWith(today)).length;
  document.getElementById('statCareer').textContent = requests.filter(r => r.category === 'Career Guidance').length;
  document.getElementById('statStudy').textContent = requests.filter(r => r.category === 'Study Resources').length;
}

/* --- Filter & Search --- */
function applyFilter() {
  const q = document.getElementById('searchInput').value.toLowerCase().trim();
  const cat = document.getElementById('categoryFilter').value;
  const filtered = allRequests.filter(r => {
    const matchCat = !cat || r.category === cat;
    const matchQ = !q || [r.name, r.category, r.message, r.email, r.whatsapp].join(' ').toLowerCase().includes(q);
    return matchCat && matchQ;
  });
  renderRequests(filtered);
}

document.getElementById('searchInput').addEventListener('input', applyFilter);
document.getElementById('categoryFilter').addEventListener('change', applyFilter);

/* --- Delete --- */
function openDeleteModal(id) {
  pendingDeleteId = id;
  document.getElementById('deleteModal').classList.add('active');
}

function closeDeleteModal() {
  pendingDeleteId = null;
  document.getElementById('deleteModal').classList.remove('active');
}

async function confirmDelete() {
  if (!pendingDeleteId) return;
  try {
    await fetch(`${ADMIN_CONFIG.apiBase}/requests/${pendingDeleteId}`, {
      method: 'DELETE',
      signal: AbortSignal.timeout(4000),
    });
  } catch (_) {}

  allRequests = allRequests.filter(r => r.id !== pendingDeleteId);
  renderRequests(allRequests);
  updateStats(allRequests);
  closeDeleteModal();
}

document.getElementById('deleteModal').addEventListener('click', (e) => {
  if (e.target === document.getElementById('deleteModal')) closeDeleteModal();
});

/* --- Export --- */
function exportRequests() {
  if (allRequests.length === 0) {
    alert('No requests to export.');
    return;
  }
  const json = JSON.stringify(allRequests, null, 2);
  const blob = new Blob([json], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `directhelp-requests-${new Date().toISOString().slice(0,10)}.json`;
  a.click();
  URL.revokeObjectURL(url);
}

/* --- Helpers --- */
function esc(str) {
  if (!str) return '';
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function formatDate(ts) {
  if (!ts) return '-';
  try {
    const d = new Date(ts);
    return d.toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' })
      + ' ' + d.toLocaleTimeString('en-IN', { hour:'2-digit', minute:'2-digit' });
  } catch (_) { return ts; }
}

function badgeClass(cat) {
  const map = {
    'Career Guidance': 'badge-career',
    'Health Guidance': 'badge-health',
    'Study Resources': 'badge-study',
    'Notes': 'badge-notes',
    'Suggestions': 'badge-suggest',
    'YouTube Links': 'badge-youtube',
    'Motivation': 'badge-motiv',
    'Other Help': 'badge-other',
  };
  return map[cat] || 'badge-other';
}

/* --- Logout --- */
function logout() {
  sessionStorage.removeItem('adminLoggedIn');
  window.location.href = 'admin-login.html';
}

/* --- Init --- */
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  loadRequests();
});
