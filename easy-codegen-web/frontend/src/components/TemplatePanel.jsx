import React, { useEffect, useState } from 'react';
import { App, Card, Input, Button, Select, Space, Divider } from 'antd';
import { SaveOutlined, FolderOpenOutlined, DeleteOutlined } from '@ant-design/icons';
import { api } from '../api';
import { useI18n } from '../i18n';

export default function TemplatePanel({ config, onSaved }) {
  const { message, modal } = App.useApp();
  const { t } = useI18n();
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

  const performSave = async (trimmed) => {
    try {
      const res = await api.saveTemplate(trimmed, config || {});
      message[res.success ? 'success' : 'error'](res.message);
      if (res.success) {
        setName('');
        loadTemplates();
      }
    } catch (e) {
      message.error(`${t('template.saveFailed')}: ${e.message}`);
    }
  };

  const handleSave = () => {
    const trimmed = name.trim();
    if (!trimmed) {
      message.warning(t('template.nameRequired'));
      return;
    }
    if (templates.includes(trimmed)) {
      modal.confirm({
        title: t('template.overwriteConfirm'),
        content: trimmed,
        okText: t('common.save'),
        cancelText: t('common.cancel'),
        onOk: () => performSave(trimmed)
      });
      return;
    }
    performSave(trimmed);
  };

  const handleLoad = async () => {
    if (!selected) {
      message.warning(t('template.selectRequired'));
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
        message.success(`${t('template.loaded')}: ${selected}`);
      } else {
        message.error(saveRes.message || t('template.loadFailed'));
      }
    } catch (e) {
      message.error(`${t('template.loadError')}: ${e.message}`);
    }
  };

  const handleDelete = () => {
    if (!selected) {
      message.warning(t('template.selectRequired'));
      return;
    }
    modal.confirm({
      title: t('template.deleteConfirm'),
      content: selected,
      okText: t('template.delete'),
      okButtonProps: { danger: true },
      cancelText: t('common.cancel'),
      onOk: async () => {
        try {
          const res = await api.deleteTemplate(selected);
          message[res.success ? 'success' : 'error'](res.message);
          if (res.success) {
            setSelected(undefined);
            loadTemplates();
          }
        } catch (e) {
          message.error(`${t('template.deleteFailed')}: ${e.message}`);
        }
      }
    });
  };

  return (
    <Card title={t('template.title')}>
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        <Space wrap>
          <Input
            placeholder={t('template.namePlaceholder')}
            style={{ width: 220 }}
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <Button type="primary" icon={<SaveOutlined />} onClick={handleSave}>
            {t('template.saveCurrent')}
          </Button>
        </Space>

        <Divider style={{ margin: 0 }} />

        <Space wrap>
          <Select
            placeholder={t('template.selectPlaceholder')}
            style={{ width: 220 }}
            value={selected}
            onChange={setSelected}
            options={templates.map((name) => ({ value: name, label: name }))}
          />
          <Button icon={<FolderOpenOutlined />} onClick={handleLoad}>
            {t('template.load')}
          </Button>
          <Button danger icon={<DeleteOutlined />} onClick={handleDelete}>
            {t('template.delete')}
          </Button>
        </Space>
      </Space>
    </Card>
  );
}
