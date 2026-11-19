import React, { useEffect, useRef, useState } from 'react';
import { App, Card, Form, Input, Button, Space, Alert, InputNumber } from 'antd';
import { ApiOutlined, SaveOutlined } from '@ant-design/icons';
import { api } from '../api';
import { useI18n } from '../i18n';

const DEFAULT_QUERY =
  'useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true';

// 从完整 db.url 中解析 主机/端口/数据库（兼容旧配置）
function parseDbUrl(url) {
  const m = /^jdbc:mysql:\/\/([^:/?#]+):(\d+)\/([^?#]*)/.exec(url || '');
  if (m) {
    return { host: m[1], port: m[2], database: m[3] };
  }
  return { host: '', port: '3306', database: '' };
}

// 用 主机/端口/数据库 拼接完整 url（保留原 query 参数）
function buildDbUrl(host, port, database, oldUrl) {
  const qIdx = (oldUrl || '').indexOf('?');
  const query = qIdx >= 0 ? oldUrl.substring(qIdx + 1) : DEFAULT_QUERY;
  return 'jdbc:mysql://' + host + ':' + port + '/' + database + '?' + query;
}

export default function ConnectionPanel({ config, onSaved }) {
  const { message } = App.useApp();
  const { t } = useI18n();
  const [form] = Form.useForm();
  const [testing, setTesting] = useState(false);
  const [saving, setSaving] = useState(false);
  const [testResult, setTestResult] = useState(null);
  const originalUrlRef = useRef('');

  useEffect(() => {
    if (config) {
      originalUrlRef.current = config['db.url'] || '';
      const { host, port, database } = parseDbUrl(config['db.url']);
      form.setFieldsValue({
        db_driver: config['db.driver.name'] || '',
        db_host: host,
        db_port: port,
        db_database: database,
        db_username: config['db.username'] || '',
        db_password: config['db.password'] || ''
      });
    }
  }, [config, form]);

  const collect = () => {
    const values = form.getFieldsValue();
    const url = buildDbUrl(
      (values.db_host || '').trim(),
      (values.db_port || 3306).toString(),
      (values.db_database || '').trim(),
      originalUrlRef.current
    );
    return {
      'db.driver.name': (values.db_driver || '').trim(),
      'db.url': url,
      'db.username': (values.db_username || '').trim(),
      'db.password': (values.db_password || '').trim()
    };
  };

  const doSave = async () => {
    setSaving(true);
    try {
      const res = await api.saveConfig(collect());
      if (res.success) {
        onSaved?.();
        return true;
      }
      return false;
    } finally {
      setSaving(false);
    }
  };

  const handleSave = async () => {
    const ok = await doSave();
    if (ok) {
      message.success(t('common.saved'));
    } else {
      message.error(t('common.saveFailed'));
    }
  };

  const handleTest = async () => {
    setTestResult(null);
    setTesting(true);
    try {
      await doSave();
      const res = await api.testConnection();
      setTestResult({ success: res.success, message: res.message });
    } catch (e) {
      setTestResult({ success: false, message: `${t('conn.testFailed')}: ${e.message}` });
    } finally {
      setTesting(false);
    }
  };

  return (
    <Card title={t('conn.title')}>
      <Form form={form} layout="vertical">
        <Form.Item name="db_driver" label={`${t('conn.driver')} (db.driver.name)`}>
          <Input placeholder="com.mysql.cj.jdbc.Driver" />
        </Form.Item>
        <Space size="large" align="start" wrap>
          <Form.Item
            name="db_host"
            label={t('conn.host')}
            rules={[{ required: true, message: t('conn.hostRequired') }]}
            style={{ minWidth: 260 }}
          >
            <Input placeholder="localhost" />
          </Form.Item>
          <Form.Item
            name="db_port"
            label={t('conn.port')}
            rules={[{ required: true, message: t('conn.portRequired') }]}
            style={{ minWidth: 120 }}
          >
            <InputNumber min={1} max={65535} style={{ width: '100%' }} placeholder="3306" />
          </Form.Item>
          <Form.Item
            name="db_database"
            label={t('conn.database')}
            rules={[{ required: true, message: t('conn.dbRequired') }]}
            style={{ minWidth: 220 }}
          >
            <Input placeholder={t('conn.databasePlaceholder')} />
          </Form.Item>
        </Space>
        <Space size="large" style={{ width: '100%' }} wrap>
          <Form.Item name="db_username" label={t('conn.username')} style={{ minWidth: 260 }}>
            <Input placeholder="root" />
          </Form.Item>
          <Form.Item name="db_password" label={t('conn.password')} style={{ minWidth: 260 }}>
            <Input.Password placeholder="******" />
          </Form.Item>
        </Space>
      </Form>
      <Space>
        <Button type="primary" icon={<ApiOutlined />} loading={testing} onClick={handleTest}>
          {t('conn.test')}
        </Button>
        <Button icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
          {t('common.save')}
        </Button>
      </Space>
      {testResult && (
        <Alert
          style={{ marginTop: 16 }}
          type={testResult.success ? 'success' : 'error'}
          showIcon
          message={testResult.message}
        />
      )}
    </Card>
  );
}
