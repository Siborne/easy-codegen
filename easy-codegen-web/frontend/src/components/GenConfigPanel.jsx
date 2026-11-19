import React, { useEffect, useState } from 'react';
import { App, Card, Form, Input, Select, Button, Row, Col } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { api } from '../api';
import { useI18n } from '../i18n';

const CONFIG_KEYS = [
  'author.comment',
  'ignore.table.prefix',
  'ignore.bean.tojson.field',
  'ignore.bean.tojson.expression',
  'ignore.bean.tojson.class',
  'bean.date.format.expression',
  'bean.date.format.class',
  'bean.date.unformat.expression',
  'bean.date.unformat.class',
  'suffix.bean.query',
  'suffix.bean.query.fuzzy',
  'suffix.bean.query.time.start',
  'suffix.bean.query.time.end',
  'suffix.mappers'
];

export default function GenConfigPanel({ config, onSaved }) {
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
      title={t('gen.title')}
      extra={
        <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
          {t('common.save')}
        </Button>
      }
    >
      <Form form={form} layout="vertical">
        <Row gutter={16}>
          <Col span={8}>
            <Form.Item name="author.comment" label={label('gen.author', 'author.comment')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="ignore.table.prefix" label={label('gen.ignoreTablePrefix', 'ignore.table.prefix')}>
              <Select
                options={[
                  { value: 'false', label: 'false' },
                  { value: 'true', label: 'true' }
                ]}
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="ignore.bean.tojson.field" label={label('gen.ignoreJsonField', 'ignore.bean.tojson.field')}>
              <Input placeholder="companyId,status" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="ignore.bean.tojson.expression" label={label('gen.ignoreAnnotation', 'ignore.bean.tojson.expression')}>
              <Input placeholder="@JsonIgnore" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="ignore.bean.tojson.class" label={label('gen.ignoreAnnotationImport', 'ignore.bean.tojson.class')}>
              <Input placeholder="import com.fasterxml.jackson.annotation.JsonIgnore" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.format.expression" label={label('gen.dateFormat', 'bean.date.format.expression')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.format.class" label={label('gen.dateFormatImport', 'bean.date.format.class')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.unformat.expression" label={label('gen.dateUnformat', 'bean.date.unformat.expression')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.unformat.class" label={label('gen.dateUnformatImport', 'bean.date.unformat.class')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query" label={label('gen.querySuffix', 'suffix.bean.query')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query.fuzzy" label={label('gen.fuzzySuffix', 'suffix.bean.query.fuzzy')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query.time.start" label={label('gen.timeStartSuffix', 'suffix.bean.query.time.start')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query.time.end" label={label('gen.timeEndSuffix', 'suffix.bean.query.time.end')}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.mappers" label={label('gen.mapperSuffix', 'suffix.mappers')}>
              <Input />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    </Card>
  );
}
