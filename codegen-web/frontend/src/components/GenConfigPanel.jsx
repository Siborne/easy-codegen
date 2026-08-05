import React, { useEffect, useState } from 'react';
import { App, Card, Form, Input, Select, Button, Row, Col } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { api } from '../api';

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
      title="生成参数"
      extra={
        <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
          保存配置
        </Button>
      }
    >
      <Form form={form} layout="vertical">
        <Row gutter={16}>
          <Col span={8}>
            <Form.Item name="author.comment" label="作者 (author.comment)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="ignore.table.prefix" label="忽略表名前缀 (ignore.table.prefix)">
              <Select
                options={[
                  { value: 'false', label: 'false' },
                  { value: 'true', label: 'true' }
                ]}
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="ignore.bean.tojson.field" label="忽略 JSON 字段 (ignore.bean.tojson.field)">
              <Input placeholder="companyId,status" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="ignore.bean.tojson.expression" label="忽略注解 (ignore.bean.tojson.expression)">
              <Input placeholder="@JsonIgnore" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="ignore.bean.tojson.class" label="忽略注解 import (ignore.bean.tojson.class)">
              <Input placeholder="import com.fasterxml.jackson.annotation.JsonIgnore" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.format.expression" label="日期序列化表达式 (bean.date.format.expression)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.format.class" label="日期序列化 import (bean.date.format.class)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.unformat.expression" label="日期反序列化表达式 (bean.date.unformat.expression)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="bean.date.unformat.class" label="日期反序列化 import (bean.date.unformat.class)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query" label="查询 Bean 后缀 (suffix.bean.query)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query.fuzzy" label="模糊搜索后缀 (suffix.bean.query.fuzzy)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query.time.start" label="开始时间后缀 (suffix.bean.query.time.start)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.bean.query.time.end" label="结束时间后缀 (suffix.bean.query.time.end)">
              <Input />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="suffix.mappers" label="Mapper 后缀 (suffix.mappers)">
              <Input />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    </Card>
  );
}
