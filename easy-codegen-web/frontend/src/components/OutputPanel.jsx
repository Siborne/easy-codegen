import React, { useEffect, useState } from 'react';
import { App, Card, Form, Input, Button, Row, Col } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { api } from '../api';
import { useI18n } from '../i18n';

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
  const { t } = useI18n();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);

  const label = (base, key) => `${t(base)} (${key})`;

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
        message.success(t('common.saved'));
      } else {
        message.error(res.message || t('common.saveFailed'));
      }
    } catch (e) {
      message.error(`${t('common.saveFailed')}: ${e.message}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card
      title={t('output.title')}
      extra={
        <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
          {t('common.save')}
        </Button>
      }
    >
      <Form form={form} layout="vertical">
        <Row gutter={16}>
          <Col span={24}>
            <Form.Item name="path.base" label={label('output.pathBase', 'path.base')}>
              <Input placeholder="./codegen-output" />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item name="package.base" label={label('output.packageBase', 'package.base')}>
              <Input placeholder="com.example" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.po" label={label('output.po', 'package.po')}>
              <Input placeholder="com.example.po" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.query" label={label('output.query', 'package.query')}>
              <Input placeholder="com.example.query" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.vo" label={label('output.vo', 'package.vo')}>
              <Input placeholder="com.example.vo" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.utils" label={label('output.utils', 'package.utils')}>
              <Input placeholder="com.example.utils" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.enums" label={label('output.enums', 'package.enums')}>
              <Input placeholder="com.example.enums" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.exception" label={label('output.exception', 'package.exception')}>
              <Input placeholder="com.example.exception" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.mappers" label={label('output.mapper', 'package.mappers')}>
              <Input placeholder="com.example.mapper" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.service" label={label('output.service', 'package.service')}>
              <Input placeholder="com.example.service" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.service.impl" label={label('output.serviceImpl', 'package.service.impl')}>
              <Input placeholder="com.example.service.impl" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="package.controller" label={label('output.controller', 'package.controller')}>
              <Input placeholder="com.example.controller" />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    </Card>
  );
}
