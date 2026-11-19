import React, { lazy, Suspense, useEffect, useState } from 'react';
import { ConfigProvider, App as AntApp, Tabs, Space, Tooltip, Spin, Alert, Button } from 'antd';
import {
  ThunderboltFilled,
  SunOutlined,
  MoonOutlined,
  DatabaseOutlined,
  SettingOutlined,
  FolderOpenOutlined,
  TableOutlined,
  RocketOutlined,
  SaveOutlined
} from '@ant-design/icons';
import zhCN from 'antd/locale/zh_CN';
import enUS from 'antd/locale/en_US';
import { theme as antTheme } from 'antd';
import { api } from './api';
import { useI18n } from './i18n';

const ConnectionPanel = lazy(() => import('./components/ConnectionPanel'));
const GenConfigPanel = lazy(() => import('./components/GenConfigPanel'));
const OutputPanel = lazy(() => import('./components/OutputPanel'));
const TablePanel = lazy(() => import('./components/TablePanel'));
const GeneratePanel = lazy(() => import('./components/GeneratePanel'));
const TemplatePanel = lazy(() => import('./components/TemplatePanel'));

const THEME_KEY = 'ecg-theme';

export default function App() {
  const { lang } = useI18n();
  const [theme, setTheme] = useState(
    () => document.documentElement.getAttribute('data-theme') || 'light'
  );

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  const isDark = theme === 'dark';

  return (
    <ConfigProvider
      locale={lang === 'en' ? enUS : zhCN}
      theme={{
        algorithm: isDark ? antTheme.darkAlgorithm : antTheme.defaultAlgorithm,
        token: {
          colorPrimary: isDark ? '#818cf8' : '#4f46e5',
          colorInfo: isDark ? '#818cf8' : '#4f46e5',
          colorLink: isDark ? '#818cf8' : '#4f46e5',
          colorBgLayout: isDark ? '#0a0e1a' : '#f7f8fc',
          colorBgContainer: isDark ? '#131b31' : '#ffffff',
          colorBgElevated: isDark ? '#131b31' : '#ffffff',
          colorText: isDark ? '#e8ecf7' : '#101828',
          colorTextSecondary: isDark ? '#a6b0c8' : '#55627a',
          colorBorder: isDark ? '#1e2740' : '#e5e9f2',
          colorBorderSecondary: isDark ? '#1e2740' : '#e5e9f2',
          borderRadius: 12,
          fontFamily:
            '"Inter", "PingFang SC", "Microsoft YaHei", system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'
        }
      }}
    >
      <AntApp>
        <Panel theme={theme} onToggleTheme={() => setTheme(isDark ? 'light' : 'dark')} />
      </AntApp>
    </ConfigProvider>
  );
}

function Panel({ theme, onToggleTheme }) {
  const { t, lang, setLang } = useI18n();
  const [config, setConfig] = useState(null);
  const [tables, setTables] = useState([]);
  const [selectedTables, setSelectedTables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  const loadConfig = () => {
    setLoading(true);
    setLoadError('');
    return api
      .getConfig()
      .then((res) => {
        if (res.success) {
          setConfig(res.data || {});
        } else {
          setLoadError(res.message || t('common.loadFailed'));
        }
      })
      .catch((e) => setLoadError(`${t('common.loadFailed')}: ${e.message}`))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadConfig();
  }, []);

  const refreshConfig = () => {
    return api.getConfig().then((res) => {
      if (res.success) setConfig(res.data || {});
      return res;
    });
  };

  const Loading = () => (
    <div className="panel-loading">
      <Spin size="large" />
    </div>
  );

  const renderBody = () => {
    if (loading) return <Loading />;
    if (loadError) {
      return (
        <Alert
          type="error"
          showIcon
          message={loadError}
          action={
            <Button size="small" onClick={loadConfig}>
              {t('common.retry')}
            </Button>
          }
        />
      );
    }
    return (
      <Tabs
        items={[
          {
            key: 'connection',
            label: (
              <Space size={6}>
                <DatabaseOutlined />
                {t('tab.connection')}
              </Space>
            ),
            children: (
              <Suspense fallback={<Loading />}>
                <ConnectionPanel config={config} onSaved={refreshConfig} />
              </Suspense>
            )
          },
          {
            key: 'genconfig',
            label: (
              <Space size={6}>
                <SettingOutlined />
                {t('tab.genConfig')}
              </Space>
            ),
            children: (
              <Suspense fallback={<Loading />}>
                <GenConfigPanel config={config} onSaved={refreshConfig} />
              </Suspense>
            )
          },
          {
            key: 'output',
            label: (
              <Space size={6}>
                <FolderOpenOutlined />
                {t('tab.output')}
              </Space>
            ),
            children: (
              <Suspense fallback={<Loading />}>
                <OutputPanel config={config} onSaved={refreshConfig} />
              </Suspense>
            )
          },
          {
            key: 'tables',
            label: (
              <Space size={6}>
                <TableOutlined />
                {t('tab.tables')}
              </Space>
            ),
            children: (
              <Suspense fallback={<Loading />}>
                <TablePanel
                  tables={tables}
                  selectedTables={selectedTables}
                  onTablesLoaded={setTables}
                  onSelectedChange={setSelectedTables}
                />
              </Suspense>
            )
          },
          {
            key: 'generate',
            label: (
              <Space size={6}>
                <RocketOutlined />
                {t('tab.generate')}
              </Space>
            ),
            children: (
              <Suspense fallback={<Loading />}>
                <GeneratePanel config={config} selectedTables={selectedTables} />
              </Suspense>
            )
          },
          {
            key: 'templates',
            label: (
              <Space size={6}>
                <SaveOutlined />
                {t('tab.templates')}
              </Space>
            ),
            children: (
              <Suspense fallback={<Loading />}>
                <TemplatePanel config={config} onSaved={refreshConfig} />
              </Suspense>
            )
          }
        ]}
      />
    );
  };

  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="container">
          <div className="brand">
            <span className="brand-mark">
              <ThunderboltFilled />
            </span>
            <span>
              Easy-Codegen
              <div className="brand-sub">{t('app.brandSub')}</div>
            </span>
          </div>
          <div className="header-right">
            <div className="lang-switch">
              <button
                type="button"
                className={lang !== 'en' ? 'lang-option active' : 'lang-option'}
                aria-pressed={lang !== 'en'}
                onClick={() => setLang('zh')}
              >
                {t('lang.zh')}
              </button>
              <button
                type="button"
                className={lang === 'en' ? 'lang-option active' : 'lang-option'}
                aria-pressed={lang === 'en'}
                onClick={() => setLang('en')}
              >
                {t('lang.en')}
              </button>
            </div>
            <Tooltip title={theme === 'dark' ? t('app.themeLight') : t('app.themeDark')}>
              <button
                type="button"
                className="icon-btn"
                aria-label={theme === 'dark' ? t('app.themeLight') : t('app.themeDark')}
                onClick={onToggleTheme}
              >
                {theme === 'dark' ? <SunOutlined /> : <MoonOutlined />}
              </button>
            </Tooltip>
          </div>
        </div>
      </header>

      <main className="panel-section">
        <div className="container">{renderBody()}</div>
      </main>

      <footer className="site-footer">
        <div className="container">
          <span>
            <strong>Easy-Codegen</strong> · {t('app.footerDesc')}
          </span>
          <span>{t('app.footerTagline')}</span>
        </div>
      </footer>
    </div>
  );
}
