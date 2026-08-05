import React, { useEffect, useState } from 'react';
import { App, Card, Input, Button, Select, Space, Divider } from 'antd';
import { SaveOutlined, FolderOpenOutlined, DeleteOutlined } from '@ant-design/icons';
import { api } from '../api';

export default function TemplatePanel({ config, onSaved }) {
  const { message, modal } = App.useApp();
  const [templates, setTemplates] = useState([]);
  const [name, setName] = useState('');
  const [selected, setSelected] = useState();

  const loadTemplates = () => {
    api.listTemplates().then((res) => {
      if (res.success) setTemplates(res.templates || []);
    });
  };

  useEffect(() => {
    loadTemplates();
  }, []);

  const handleSave = async () => {
    if (!name.trim()) {
      message.warning('请输入模板名称');
      return;
    }
    try {
      const res = await api.saveTemplate(name.trim(), config || {});
      message[res.success ? 'success' : 'error'](res.message);
      if (res.success) {
        setName('');
        loadTemplates();
      }
    } catch (e) {
      message.error('保存模板失败: ' + e.message);
    }
  };

  const handleLoad = async () => {
    if (!selected) {
      message.warning('请先选择模板');
      return;
    }
    try {
      const res = await api.loadTemplate(selected);
      if (!res.success) {
        message.error(res.message);
        return;
      }
      const saveRes = await api.saveConfig(res.data || {});
      if (saveRes.success) {
        onSaved?.();
        message.success('模板已加载并生效: ' + selected);
      } else {
        message.error(saveRes.message || '加载失败');
      }
    } catch (e) {
      message.error('加载模板失败: ' + e.message);
    }
  };

  const handleDelete = () => {
    if (!selected) {
      message.warning('请先选择模板');
      return;
    }
    modal.confirm({
      title: '确认删除模板？',
      content: selected,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await api.deleteTemplate(selected);
          message[res.success ? 'success' : 'error'](res.message);
          if (res.success) {
            setSelected(undefined);
            loadTemplates();
          }
        } catch (e) {
          message.error('删除失败: ' + e.message);
        }
      }
    });
  };

  return (
    <Card title="配置模板（保存/加载多套生成配置）">
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        <Space wrap>
          <Input
            placeholder="模板名称，如: 测试环境"
            style={{ width: 220 }}
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <Button type="primary" icon={<SaveOutlined />} onClick={handleSave}>
            保存当前配置为模板
          </Button>
        </Space>

        <Divider style={{ margin: 0 }} />

        <Space wrap>
          <Select
            placeholder="选择模板"
            style={{ width: 220 }}
            value={selected}
            onChange={setSelected}
            options={templates.map((t) => ({ value: t, label: t }))}
          />
          <Button icon={<FolderOpenOutlined />} onClick={handleLoad}>
            加载模板
          </Button>
          <Button danger icon={<DeleteOutlined />} onClick={handleDelete}>
            删除模板
          </Button>
        </Space>
      </Space>
    </Card>
  );
}
