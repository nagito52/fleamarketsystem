/**
 * 画像トリミング機能
 * Cropper.js を使用した画像切り抜きモジュール
 */

(function () {
  'use strict';

  let cropper = null;
  let originalFile = null;

  document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('image');
    if (!fileInput) return;

    // モーダルHTML を動的に挿入
    createCropModal();

    // ファイル選択時にトリミングモーダルを表示
    fileInput.addEventListener('change', handleFileSelect);
  });

  /**
   * トリミングモーダルのHTMLを生成
   */
  function createCropModal() {
    const modal = document.createElement('div');
    modal.id = 'crop-modal';
    modal.className = 'crop-modal-overlay';
    modal.innerHTML = `
      <div class="crop-modal">
        <div class="crop-modal-header">
          <h2><i class="fas fa-crop-alt"></i> 画像をトリミング</h2>
          <p>ドラッグして切り抜き範囲を調整してください</p>
        </div>
        <div class="crop-modal-body">
          <div class="crop-container">
            <img id="crop-image" src="" alt="トリミング対象">
          </div>
        </div>
        <div class="crop-modal-controls">
          <div class="crop-toolbar">
            <button type="button" class="crop-tool-btn" id="crop-rotate-left" title="左に回転">
              <i class="fas fa-undo"></i>
            </button>
            <button type="button" class="crop-tool-btn" id="crop-rotate-right" title="右に回転">
              <i class="fas fa-redo"></i>
            </button>
            <button type="button" class="crop-tool-btn" id="crop-flip-h" title="左右反転">
              <i class="fas fa-arrows-alt-h"></i>
            </button>
            <button type="button" class="crop-tool-btn" id="crop-flip-v" title="上下反転">
              <i class="fas fa-arrows-alt-v"></i>
            </button>
            <button type="button" class="crop-tool-btn" id="crop-reset" title="リセット">
              <i class="fas fa-sync-alt"></i>
            </button>
          </div>
          <div class="crop-aspect-buttons">
            <button type="button" class="crop-aspect-btn active" data-aspect="free">フリー</button>
            <button type="button" class="crop-aspect-btn" data-aspect="1">1:1</button>
            <button type="button" class="crop-aspect-btn" data-aspect="4/3">4:3</button>
            <button type="button" class="crop-aspect-btn" data-aspect="16/9">16:9</button>
          </div>
        </div>
        <div class="crop-modal-footer">
          <button type="button" class="btn btn-secondary" id="crop-cancel">
            <i class="fas fa-times"></i> キャンセル
          </button>
          <button type="button" class="btn btn-primary" id="crop-confirm">
            <i class="fas fa-check"></i> トリミングを適用
          </button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);

    // イベントリスナー設定
    document.getElementById('crop-cancel').addEventListener('click', closeCropModal);
    document.getElementById('crop-confirm').addEventListener('click', applyCrop);
    document.getElementById('crop-rotate-left').addEventListener('click', () => cropper && cropper.rotate(-90));
    document.getElementById('crop-rotate-right').addEventListener('click', () => cropper && cropper.rotate(90));
    document.getElementById('crop-flip-h').addEventListener('click', () => {
      if (!cropper) return;
      const data = cropper.getData();
      cropper.scaleX(data.scaleX === -1 ? 1 : -1);
    });
    document.getElementById('crop-flip-v').addEventListener('click', () => {
      if (!cropper) return;
      const data = cropper.getData();
      cropper.scaleY(data.scaleY === -1 ? 1 : -1);
    });
    document.getElementById('crop-reset').addEventListener('click', () => cropper && cropper.reset());

    // アスペクト比ボタン
    document.querySelectorAll('.crop-aspect-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.crop-aspect-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        if (!cropper) return;
        const aspect = btn.dataset.aspect;
        if (aspect === 'free') {
          cropper.setAspectRatio(NaN);
        } else if (aspect.includes('/')) {
          const [w, h] = aspect.split('/').map(Number);
          cropper.setAspectRatio(w / h);
        } else {
          cropper.setAspectRatio(Number(aspect));
        }
      });
    });

    // モーダル外クリックで閉じる
    modal.addEventListener('click', (e) => {
      if (e.target === modal) closeCropModal();
    });

    // Escキーで閉じる
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && modal.classList.contains('active')) {
        closeCropModal();
      }
    });
  }

  /**
   * ファイル選択ハンドラー
   */
  function handleFileSelect(e) {
    const file = e.target.files[0];
    if (!file) return;

    // 画像ファイルチェック
    if (!file.type.startsWith('image/')) {
      showToast('画像ファイルを選択してください', 'error');
      e.target.value = '';
      return;
    }

    // ファイルサイズチェック (10MB - トリミング前は大きめに許容)
    if (file.size > 10 * 1024 * 1024) {
      showToast('画像サイズは10MB以下にしてください', 'error');
      e.target.value = '';
      return;
    }

    originalFile = file;

    const reader = new FileReader();
    reader.onload = (event) => {
      openCropModal(event.target.result);
    };
    reader.readAsDataURL(file);
  }

  /**
   * トリミングモーダルを開く
   */
  function openCropModal(imageSrc) {
    const modal = document.getElementById('crop-modal');
    const cropImage = document.getElementById('crop-image');

    cropImage.src = imageSrc;
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';

    // 既存のcropperを破棄
    if (cropper) {
      cropper.destroy();
      cropper = null;
    }

    // Cropper.js 初期化（画像読み込み後）
    cropImage.onload = () => {
      cropper = new Cropper(cropImage, {
        viewMode: 1,
        dragMode: 'move',
        aspectRatio: NaN,   // フリー
        autoCropArea: 0.9,
        responsive: true,
        restore: false,
        guides: true,
        center: true,
        highlight: true,
        cropBoxMovable: true,
        cropBoxResizable: true,
        toggleDragModeOnDblclick: true,
        background: true,
      });
    };

    // アスペクト比ボタンをリセット
    document.querySelectorAll('.crop-aspect-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('.crop-aspect-btn[data-aspect="free"]').classList.add('active');
  }

  /**
   * トリミングモーダルを閉じる
   */
  function closeCropModal() {
    const modal = document.getElementById('crop-modal');
    modal.classList.remove('active');
    document.body.style.overflow = '';

    if (cropper) {
      cropper.destroy();
      cropper = null;
    }

    // ファイル入力をリセット
    const fileInput = document.getElementById('image');
    if (fileInput) {
      fileInput.value = '';
    }
  }

  /**
   * トリミングを適用
   */
  function applyCrop() {
    if (!cropper) return;

    const confirmBtn = document.getElementById('crop-confirm');
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 処理中...';

    // Canvas からトリミング結果を取得
    const canvas = cropper.getCroppedCanvas({
      maxWidth: 1200,
      maxHeight: 1200,
      imageSmoothingEnabled: true,
      imageSmoothingQuality: 'high',
    });

    if (!canvas) {
      showToast('トリミングに失敗しました', 'error');
      confirmBtn.disabled = false;
      confirmBtn.innerHTML = '<i class="fas fa-check"></i> トリミングを適用';
      return;
    }

    // Canvas を Blob に変換
    canvas.toBlob((blob) => {
      if (!blob) {
        showToast('画像の変換に失敗しました', 'error');
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = '<i class="fas fa-check"></i> トリミングを適用';
        return;
      }

      // ファイル名を保持しつつ新しいFileオブジェクトを生成
      const fileName = originalFile ? originalFile.name : 'cropped-image.jpg';
      const croppedFile = new File([blob], fileName, {
        type: blob.type || 'image/jpeg',
        lastModified: Date.now(),
      });

      // DataTransfer を使ってファイル入力に設定
      const dataTransfer = new DataTransfer();
      dataTransfer.items.add(croppedFile);
      const fileInput = document.getElementById('image');
      fileInput.files = dataTransfer.files;

      // プレビューを更新
      updatePreview(canvas.toDataURL('image/jpeg', 0.9));

      // モーダルを閉じる
      const modal = document.getElementById('crop-modal');
      modal.classList.remove('active');
      document.body.style.overflow = '';

      if (cropper) {
        cropper.destroy();
        cropper = null;
      }

      confirmBtn.disabled = false;
      confirmBtn.innerHTML = '<i class="fas fa-check"></i> トリミングを適用';

      showToast('画像をトリミングしました！', 'success');
    }, 'image/jpeg', 0.9);
  }

  /**
   * プレビュー画像を更新
   */
  function updatePreview(dataUrl) {
    const fileInput = document.getElementById('image');
    let preview = document.getElementById('image-preview');

    if (!preview) {
      preview = document.createElement('div');
      preview.id = 'image-preview';
      fileInput.parentNode.appendChild(preview);
    }

    preview.className = 'cropped-preview';
    preview.innerHTML = `
      <div class="cropped-preview-header">
        <i class="fas fa-check-circle"></i> トリミング済み
      </div>
      <img src="${dataUrl}" alt="トリミング済みプレビュー">
      <button type="button" class="cropped-preview-change" onclick="document.getElementById('image').click()">
        <i class="fas fa-redo"></i> 画像を変更
      </button>
    `;
  }

  /**
   * トースト表示（app.jsのshowToastが無い場合のフォールバック）
   */
  if (typeof window.showToast !== 'function') {
    window.showToast = function (message, type) {
      const toast = document.createElement('div');
      toast.style.cssText = `
        position: fixed; bottom: 24px; right: 24px; padding: 14px 24px;
        border-radius: 8px; font-size: 0.9rem; font-weight: 500; z-index: 100001;
        transform: translateY(20px); opacity: 0; transition: all 0.3s ease;
        max-width: 400px; box-shadow: 0 4px 16px rgba(0,0,0,0.15);
      `;
      const colors = {
        success: '#00B894', error: '#E74C3C', warning: '#F39C12', info: '#4ECDC4'
      };
      toast.style.background = colors[type] || colors.info;
      toast.style.color = '#fff';
      toast.textContent = message;
      document.body.appendChild(toast);
      requestAnimationFrame(() => { toast.style.transform = 'translateY(0)'; toast.style.opacity = '1'; });
      setTimeout(() => {
        toast.style.transform = 'translateY(20px)'; toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
      }, 3000);
    };
  }

})();
