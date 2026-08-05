import React, { useEffect, useState } from 'react';
import { ConfigProvider, App as AntApp, Layout, Tabs, Typography, Tag, Space, Button, Tooltip } from 'antd';
import { CodeOutlined, SunOutlined, MoonOutlined } from '@ant-design/icons';
import zhCN from 'antd/locale/zh_CN';
import { theme as antTheme } from 'antd';
import { api } from './api';
import ConnectionPanel from './components/ConnectionPanel';
import GenConfigPanel from './components/GenConfigPanel';
import OutputPanel from './components/OutputPanel';
import TablePanel from './components/TablePanel';
import GeneratePanel from './components/GeneratePanel';
import TemplatePanel from './components/TemplatePanel';

const { Header, Content } = Layout;
const THEME_KEY = 'easy-codegen-theme';

export default function App() {
  const [theme, setTheme] = useState(() => localStorage.getItem(THEME_KEY) || 'light');

  useEffect(() => {
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  return (
    <ConfigProvider
      locale={zhCN}
      theme={{ algorithm: theme === 'dark' ? antTheme.darkAlgorithm : antTheme.defaultAlgorithm }}
    >
      <AntApp>
        <Panel theme={theme} onToggleTheme={() => setTheme(theme === 'dark' ? 'light' : 'dark')} />
      </AntApp>
    </ConfigProvider>
  );
}

function Panel({ theme, onToggleTheme }) {
  const [config, setConfig] = useState(null);
  const [configFile, setConfigFile] = useState('');
  const [tables, setTables] = useState([]);
  const [selectedTables, setSelectedTables] = useState([]);

  useEffect(() => {
    api.getConfig().then((res) => {
      if (res.success) {
        setConfig(res.data || {});
        setConfigFile(res.file || '');
      }
    });
  }, []);

  const refreshConfig = () => {
    return api.getConfig().then((res) => {
      if (res.success) setConfig(res.data || {});
      return res;
    });
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Space align="center">
          <CodeOutlined style={{ color: '#40a9ff', fontSize: 24 }} />
          <Typography.Title level={4} style={{ color: '#fff', margin: 0 }}>
            Easy-Codegen 可视化面板
          </Typography.Title>
        </Space>
        <Space>
          {configFile && (
            <Tag color="blue" style={{ fontSize: 12, maxWidth: 480, overflow: 'hidden', textOverflow: 'ellipsis' }}>
              配置文件: {configFile}
            </Tag>
          )}
          <Tooltip title={theme === 'dark' ? '切换到亮色' : '切换到暗色'}>
            <Button
              type="text"
              icon={theme === 'dark' ? <SunOutlined /> : <MoonOutlined />}
              onClick={onToggleTheme}
              style={{ color: '#fff' }}
            />
          </Tooltip>
        </Space>
      </Header>
      <Content style={{ padding: 24, maxWidth: 1280, width: '100%', margin: '0 auto' }}>
        <Tabs
          items={[
            {
              key: 'connection',
              label: '数据库连接',
              children: <ConnectionPanel config={config} onSaved={refreshConfig} />
            },
            {
              key: 'genconfig',
              label: '生成参数',
              children: <GenConfigPanel config={config} onSaved={refreshConfig} />
            },
            {
              key: 'output',
              label: '输出与包名',
              children: <OutputPanel config={config} onSaved={refreshConfig} />
            },
            {
              key: 'tables',
              label: '数据表',
              children: (
                <TablePanel
                  tables={tables}
                  selectedTables={selectedTables}
                  onTablesLoaded={setTables}
                  onSelectedChange={setSelectedTables}
                />
              )
            },
            {
              key: 'generate',
              label: '代码生成',
              children: <GeneratePanel config={config} selectedTables={selectedTables} />
            },
            {
              key: 'templates',
              label: '配置模板',
              children: <TemplatePanel config={config} onSaved={refreshConfig} />
            }
          ]}
        />
      </Content>
    </Layout>
  );
}
