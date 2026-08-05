import React, { useEffect, useState } from 'react';
import { App, Card, Form, Input, Button, Space, Alert } from 'antd';
import { ApiOutlined, SaveOutlined } from '@ant-design/icons';
import { api } from '../api';

const CONFIG_KEYS = ['db.driver.name', 'db.url', 'db.username', 'db.password'];

export default function ConnectionPanel({ config, onSaved }) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [testing, setTesting] = useState(false);
  const [saving, setSaving] = useState(false);
  const [testResult, setTestResult] = useState(null);

  useEffect(() => {
    if (config) {
      const values = {};
      CONFIG_KEYS.forEach((k) => (values[k] = config[k] || ''));
      form.setFieldsValue(values);
    }
  }, [config, form]);

  const collect = () => {
    const values = form.getFieldsValue();
    const data = {};
    CONFIG_KEYS.forEach((k) => (data[k] = (values[k] || '').trim()));
    return data;
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
      message.success('配置已保存并生效');
    } else {
      message.error('保存配置失败');
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
      setTestResult({ success: false, message: '测试失败: ' + e.message });
    } finally {
      setTesting(false);
    }
  };

  return (
    <Card title="数据库连接">
      <Form form={form} layout="vertical">
        <Form.Item name="db.driver.name" label="驱动类 (db.driver.name)">
          <Input placeholder="com.mysql.cj.jdbc.Driver" />
        </Form.Item>
        <Form.Item name="db.url" label="连接 URL (db.url)">
          <Input placeholder="jdbc:mysql://localhost:3306/dbname?..." />
        </Form.Item>
        <Space size="large" style={{ width: '100%' }} wrap>
          <Form.Item name="db.username" label="用户名 (db.username)" style={{ minWidth: 260 }}>
            <Input placeholder="root" />
          </Form.Item>
          <Form.Item name="db.password" label="密码 (db.password)" style={{ minWidth: 260 }}>
            <Input.Password placeholder="******" />
          </Form.Item>
        </Space>
      </Form>
      <Space>
        <Button type="primary" icon={<ApiOutlined />} loading={testing} onClick={handleTest}>
          测试连接
        </Button>
        <Button icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
          保存配置
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
