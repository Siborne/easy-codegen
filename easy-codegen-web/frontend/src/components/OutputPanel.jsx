import React, { useEffect, useState } from 'react';
import { App, Card, Form, Input, Button, Row, Col } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { api } from '../api';

const CONFIG_KEYS = [
  'path.base',
  'package.base',
  'package.po',
  'package.query',
  'package.vo',
  'package.utils',
  'package.enums',
  'package.exception',
  'package.mappers',
  'package.service',
  'package.service.impl',
  'package.controller'
];

export default function OutputPanel({ config, onSaved }) {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (config) {
      const values = {};
      CONFIG_KEYS.forEach((k) => (values[k] = config[k] || ''));
      form.setFieldsValue(values);
    }
  }, [config, form]);

  const handleSave = async () => {
    setSaving(true);
    try {
      const values = form.getFieldsValue();
      const data = {};
      CONFIG_KEYS.forEach((k) => (data[k] = (values[k] || '').trim()));
      const res = await api.saveConfig(data);
      if (res.success) {
        onSaved?.();
        message.success('配置已保存并生效');
      } else {
        message.error(res.message || '保存失败');
      }
    } catch (e) {
      message.error('保存失败: ' + e.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card
      title="输出与包名"
      extra={
        <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
          保存配置
        </Button>
      }
    >
      <Form form={form} layout="vertical">
        <Row gutter={16}>
          <Col span={24}>
            <Form.Item name="path.base" label="输出根路径 (path.base)">
              <Input placeholder="D:/workspace/codegen-output/" />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item name="package.base" label="根包名 (package.base)">
              <Input placeholder="org.flyingSparrow" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.po" label="PO 包 (package.po)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.query" label="Query 包 (package.query)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.vo" label="VO 包 (package.vo)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.utils" label="工具包 (package.utils)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.enums" label="枚举包 (package.enums)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.exception" label="异常包 (package.exception)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.mappers" label="Mapper 包 (package.mappers)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.service" label="Service 包 (package.service)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.service.impl" label="ServiceImpl 包 (package.service.impl)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.controller" label="Controller 包 (package.controller)">
              <Input />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    </Card>
  );
}
