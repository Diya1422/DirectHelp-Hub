/* ============================================
   DIRECTHELP HUB - MAIN JAVASCRIPT
   ============================================ */

const CONFIG = {
  // TODO: Replace with your actual WhatsApp number (country code + number, no + or spaces)
  // Example: India +91 98765 43210 -> '919876543210'
  adminWhatsApp: '7303208871',
  apiBase: 'http://localhost:9090/api',
  maxChars: 500,
};

/* --- Theme --- */
function initTheme() {
  const saved = localStorage.getItem('theme') || 'dark';
  document.body.className = saved + '-mode';
  updateThemeIcon(saved);
}

function toggleTheme() {
  const isDark = document.body.classList.contains('dark-mode');
  const next = isDark ? 'light' : 'dark';
  document.body.className = next + '-mode';
  localStorage.setItem('theme', next);
  updateThemeIcon(next);
}

function updateThemeIcon(theme) {
  const icon = document.getElementById('themeIcon');
  if (!icon) return;
  icon.className = theme === 'dark' ? 'fas fa-moon' : 'fas fa-sun';
}

/* --- Navbar --- */
function initNavbar() {
  const navbar = document.getElementById('navbar');
  const hamburger = document.getElementById('hamburger');
  const navMenu = document.getElementById('navMenu');
  const themeToggle = document.getElementById('themeToggle');

  // Sticky shadow on scroll
  window.addEventListener('scroll', () => {
    navbar.classList.toggle('scrolled', window.scrollY > 20);
    document.getElementById('scrollTop').classList.toggle('visible', window.scrollY > 400);
  });

  // Hamburger toggle
  hamburger.addEventListener('click', () => {
    hamburger.classList.toggle('active');
    navMenu.classList.toggle('open');
  });

  // Close menu on nav link click
  navMenu.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', () => {
      hamburger.classList.remove('active');
      navMenu.classList.remove('open');
    });
  });

  // Mobile dropdown toggle
  document.querySelectorAll('.has-dropdown').forEach(item => {
    item.querySelector('.dropdown-toggle').addEventListener('click', (e) => {
      if (window.innerWidth <= 768) {
        e.preventDefault();
        item.classList.toggle('open');
      }
    });
  });

  // Dropdown category links -> pre-select form category
  document.querySelectorAll('[data-cat]').forEach(link => {
    link.addEventListener('click', () => {
      selectCategory(link.getAttribute('data-cat'));
    });
  });

  themeToggle.addEventListener('click', toggleTheme);

  // Scroll to top
  document.getElementById('scrollTop').addEventListener('click', () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });

  // Active nav link on scroll
  const sections = document.querySelectorAll('section[id]');
  const navLinks = document.querySelectorAll('.nav-link:not(.dropdown-toggle)');
  window.addEventListener('scroll', () => {
    let current = '';
    sections.forEach(sec => {
      if (window.scrollY >= sec.offsetTop - 100) current = sec.id;
    });
    navLinks.forEach(link => {
      link.style.color = '';
      if (link.getAttribute('href') === '#' + current) link.style.color = 'var(--primary)';
    });
  });
}

/* --- Category pre-select --- */
function selectCategory(cat) {
  const sel = document.getElementById('category');
  if (sel) sel.value = cat;
}

/* --- Form --- */
function initForm() {
  const form = document.getElementById('helpForm');
  const msgArea = document.getElementById('message');
  const charCount = document.getElementById('charCount');

  // Character counter
  msgArea.addEventListener('input', () => {
    const len = msgArea.value.length;
    charCount.textContent = len;
    charCount.style.color = len > CONFIG.maxChars ? 'var(--danger)' : '';
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    const data = {
      name: document.getElementById('fullName').value.trim(),
      whatsapp: document.getElementById('whatsappNum').value.trim(),
      email: document.getElementById('emailAddr').value.trim(),
      category: document.getElementById('category').value,
      message: document.getElementById('message').value.trim(),
    };

    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';

    // Try saving to Java backend (optional - works without server)
    try {
      await fetch(CONFIG.apiBase + '/requests', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
        signal: AbortSignal.timeout(3000),
      });
    } catch (_) {
      // Server not running - proceed anyway
    }

    // Always redirect to WhatsApp
    sendToWhatsApp(data);

    btn.disabled = false;
    btn.innerHTML = '<i class="fab fa-whatsapp"></i> Send via WhatsApp';
    form.reset();
    charCount.textContent = '0';
    showModal();
  });
}

function validateForm() {
  let valid = true;
  const fields = [
    { id: 'fullName', err: 'nameErr', msg: 'Please enter your full name.' },
    { id: 'whatsappNum', err: 'waErr', msg: 'Please enter your WhatsApp number.' },
    { id: 'category', err: 'catErr', msg: 'Please select a category.' },
    { id: 'message', err: 'msgErr', msg: 'Please describe your request.' },
  ];

  fields.forEach(f => {
    const el = document.getElementById(f.id);
    const err = document.getElementById(f.err);
    const val = el.value.trim();
    if (!val) {
      err.textContent = f.msg;
      el.style.borderColor = 'var(--danger)';
      valid = false;
    } else {
      err.textContent = '';
      el.style.borderColor = '';
    }
  });

  // WhatsApp number format check
  const wa = document.getElementById('whatsappNum').value.trim().replace(/\D/g, '');
  if (wa && wa.length < 10) {
    document.getElementById('waErr').textContent = 'Enter a valid phone number (min 10 digits).';
    document.getElementById('whatsappNum').style.borderColor = 'var(--danger)';
    valid = false;
  }

  // Email format check (optional field)
  const email = document.getElementById('emailAddr').value.trim();
  const emailErr = document.getElementById('emailErr');
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    emailErr.textContent = 'Enter a valid email address.';
    document.getElementById('emailAddr').style.borderColor = 'var(--danger)';
    valid = false;
  } else {
    emailErr.textContent = '';
    document.getElementById('emailAddr').style.borderColor = '';
  }

  // Message length
  const msg = document.getElementById('message').value.trim();
  if (msg.length > CONFIG.maxChars) {
    document.getElementById('msgErr').textContent = `Message too long (max ${CONFIG.maxChars} characters).`;
    valid = false;
  }

  return valid;
}

function sendToWhatsApp(data) {
  const msg = [
    '*New Help Request - DirectHelp Hub*',
    '',
    `*Name:* ${data.name}`,
    `*WhatsApp:* ${data.whatsapp}`,
    `*Email:* ${data.email || 'Not provided'}`,
    `*Category:* ${data.category}`,
    '',
    '*Request:*',
    data.message,
  ].join('\n');

  const url = `https://wa.me/${CONFIG.adminWhatsApp}?text=${encodeURIComponent(msg)}`;
  window.open(url, '_blank');
}

/* --- Modal --- */
function showModal() {
  document.getElementById('modalOverlay').classList.add('active');
}

function closeModal() {
  document.getElementById('modalOverlay').classList.remove('active');
}

// Close modal on overlay click
document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('modalOverlay').addEventListener('click', (e) => {
    if (e.target === document.getElementById('modalOverlay')) closeModal();
  });
});

/* --- FAQ --- */
function toggleFaq(el) {
  const item = el.closest('.faq-item');
  const allItems = document.querySelectorAll('.faq-item');
  allItems.forEach(i => { if (i !== item) i.classList.remove('open'); });
  item.classList.toggle('open');
}

/* --- Resource Search --- */
function initResourceSearch() {
  const input = document.getElementById('resourceSearch');
  if (!input) return;
  input.addEventListener('input', () => {
    const q = input.value.toLowerCase().trim();
    document.querySelectorAll('.res-card').forEach(card => {
      const text = (card.textContent + card.getAttribute('data-tags')).toLowerCase();
      card.classList.toggle('hidden', q.length > 0 && !text.includes(q));
    });
  });
}

/* --- Scroll Animations --- */
function initAnimations() {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('aos-visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });

  document.querySelectorAll('[data-aos]').forEach(el => observer.observe(el));
}

/* --- Init --- */
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initNavbar();
  initForm();
  initResourceSearch();
  initAnimations();
});
