/**
 * フリマアプリ - メインJavaScript
 * UI インタラクション、アニメーション、ユーティリティ
 */

document.addEventListener('DOMContentLoaded', () => {
  initAnimations();
  initAutoHideAlerts();
  initImagePreview();
  initConfirmDialogs();
  initTextareaAutoResize();
  initScrollToChat();
});

/* --- フェードインアニメーション --- */
function initAnimations() {
  const elements = document.querySelectorAll('.animate-in');
  if (!elements.length) return;

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry, index) => {
      if (entry.isIntersecting) {
        setTimeout(() => {
          entry.target.style.animationDelay = `${index * 0.05}s`;
          entry.target.classList.add('visible');
        }, index * 50);
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });

  elements.forEach(el => observer.observe(el));
}

/* --- アラート自動非表示 --- */
function initAutoHideAlerts() {
  const alerts = document.querySelectorAll('.alert-success, .success-message, .alert-danger, .error-message');
  alerts.forEach(alert => {
    // 5秒後にフェードアウト
    setTimeout(() => {
      alert.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
      alert.style.opacity = '0';
      alert.style.transform = 'translateY(-10px)';
      setTimeout(() => {
        alert.style.display = 'none';
      }, 500);
    }, 5000);
  });
}

/* --- 画像プレビュー --- */
function initImagePreview() {
  const fileInput = document.getElementById('image');
  if (!fileInput) return;

  fileInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // ファイルサイズチェック (5MB)
    if (file.size > 5 * 1024 * 1024) {
      showToast('画像サイズは5MB以下にしてください', 'error');
      fileInput.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      let preview = document.getElementById('image-preview');
      if (!preview) {
        preview = document.createElement('div');
        preview.id = 'image-preview';
        preview.style.cssText = 'margin-top: 12px; border-radius: 8px; overflow: hidden; max-width: 300px;';
        fileInput.parentNode.appendChild(preview);
      }
      preview.innerHTML = `<img src="${event.target.result}" style="width: 100%; height: auto; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);" alt="プレビュー">`;
    };
    reader.readAsDataURL(file);
  });
}

/* --- 確認ダイアログのスタイル改善 --- */
function initConfirmDialogs() {
  // confirm属性を持つボタンにカスタムスタイルを適用
  document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', (e) => {
      const message = el.getAttribute('data-confirm');
      if (!confirm(message)) {
        e.preventDefault();
      }
    });
  });
}

/* --- Textarea自動リサイズ --- */
function initTextareaAutoResize() {
  document.querySelectorAll('textarea').forEach(textarea => {
    textarea.addEventListener('input', function() {
      this.style.height = 'auto';
      this.style.height = Math.min(this.scrollHeight, 200) + 'px';
    });
  });
}

/* --- チャットセクションへスクロール --- */
function initScrollToChat() {
  if (window.location.hash === '#chat-section') {
    const chatSection = document.getElementById('chat-section');
    if (chatSection) {
      setTimeout(() => {
        chatSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 300);
    }
  }

  // チャットボックスを最下部にスクロール
  const chatBox = document.querySelector('.chat-box');
  if (chatBox && chatBox.children.length > 0) {
    chatBox.scrollTop = chatBox.scrollHeight;
  }
}

/* --- トースト通知 --- */
function showToast(message, type = 'info') {
  const toast = document.createElement('div');
  toast.style.cssText = `
    position: fixed;
    bottom: 24px;
    right: 24px;
    padding: 14px 24px;
    border-radius: 8px;
    font-size: 0.9rem;
    font-weight: 500;
    z-index: 10000;
    transform: translateY(20px);
    opacity: 0;
    transition: all 0.3s ease;
    max-width: 400px;
    box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  `;

  const colors = {
    success: { bg: '#00B894', color: '#fff' },
    error: { bg: '#E74C3C', color: '#fff' },
    warning: { bg: '#F39C12', color: '#fff' },
    info: { bg: '#4ECDC4', color: '#fff' }
  };

  const c = colors[type] || colors.info;
  toast.style.background = c.bg;
  toast.style.color = c.color;
  toast.textContent = message;

  document.body.appendChild(toast);

  requestAnimationFrame(() => {
    toast.style.transform = 'translateY(0)';
    toast.style.opacity = '1';
  });

  setTimeout(() => {
    toast.style.transform = 'translateY(20px)';
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

/* --- スムーズスクロール --- */
function smoothScrollTo(elementId) {
  const element = document.getElementById(elementId);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}
