// 1. Password show/hide
const togglePassword = document.getElementById('togglePassword');
const passwordInput = document.getElementById('password');
if (togglePassword && passwordInput) {
  togglePassword.addEventListener('change', function () {
    passwordInput.type = this.checked ? 'text' : 'password';
  });
}

// 2. Expiry date auto-calculate (Add Member page)
const planSelect = document.getElementById('plan');
const startDateInput = document.getElementById('startDate');
const expiryDateInput = document.getElementById('expiryDate');

function formatDate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function addMonths(dateStr, months) {
  const d = new Date(dateStr + 'T00:00:00');
  const day = d.getDate();
  d.setDate(1);
  d.setMonth(d.getMonth() + months);
  const lastDay = new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
  d.setDate(Math.min(day, lastDay));
  return d;
}

function updateExpiry() {
  if (!startDateInput.value) return;
  const months = parseInt(planSelect.selectedOptions[0].dataset.months);
  expiryDateInput.value = formatDate(addMonths(startDateInput.value, months));
}

if (planSelect && startDateInput && expiryDateInput) {
  planSelect.addEventListener('change', updateExpiry);
  startDateInput.addEventListener('change', updateExpiry);
  updateExpiry();      // ← nayi line: Renew page par start date pehle se bhari hoti hai
}

// 3. Photo preview
const photoInput = document.getElementById('photo');
const preview = document.getElementById('preview');
if (photoInput && preview) {
  photoInput.addEventListener('change', function () {
    const file = this.files[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      alert('Photo 2MB se chhoti honi chahiye');
      this.value = '';
      return;
    }
    preview.src = URL.createObjectURL(file);
    preview.style.display = 'block';
  });
}

// 4. Mobile number validation (Add Member form)
const memberForm = document.getElementById('memberForm');
const mobileInput = document.querySelector('input[name="mobile"]');
if (memberForm && mobileInput) {
  memberForm.addEventListener('submit', function (e) {
    if (!/^[6-9]\d{9}$/.test(mobileInput.value)) {
      e.preventDefault();
      alert('Sahi 10 digit ka mobile number daalein');
    }
  });
}