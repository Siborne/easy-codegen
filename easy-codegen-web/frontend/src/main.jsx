import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles.css';
import App from './App';
import { LangProvider } from './i18n';

// 首帧前写入主题，避免闪烁（与 landing 使用相同的 localStorage key）
(function initTheme() {
  try {
    const stored = localStorage.getItem('ecg-theme');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const dark = stored ? stored === 'dark' : prefersDark;
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
  } catch (e) {
    document.documentElement.setAttribute('data-theme', 'light');
  }
})();

ReactDOM.createRoot(document.getElementById('root')).render(
  <LangProvider>
    <App />
  </LangProvider>
);
