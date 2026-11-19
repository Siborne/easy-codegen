import React, { useState } from 'react';
import {
  App,
  Card,
  Button,
  Alert,
  List,
  Space,
  Tag,
  Typography,
  Checkbox,
  Divider,
  Drawer,
  Select,
  Tooltip
} from 'antd';
import {
  RocketOutlined,
  EyeOutlined,
  DownloadOutlined,
  CopyOutlined,
  CheckOutlined
} from '@ant-design/icons';
import { api } from '../api';
import { useI18n } from '../i18n';

const TYPE_OPTIONS = [
  { value: 'base', key: 'type.base' },
  { value: 'po', key: 'type.po' },
  { value: 'query', key: 'type.query' },
  { value: 'mapper', key: 'type.mapper' },
  { value: 'mapperXml', key: 'type.mapperXml' },
  { value: 'service', key: 'type.service' },
  { value: 'serviceImpl', key: 'type.serviceImpl' },
  { value: 'controller', key: 'type.controller' }
];
const ALL_TYPES = TYPE_OPTIONS.map((t) => t.value);

export default function GeneratePanel({ config, selectedTables }) {
  const { message, modal } = App.useApp();
  const { t } = useI18n();
  const [types, setTypes] = useState(ALL_TYPES);
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState(null); // { success, message, files }
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewing, setPreviewing] = useState(false);
  const [previewFiles, setPreviewFiles] = useState([]);
  const [previewIndex, setPreviewIndex] = useState(0);
  const [copied, setCopied] = useState(false);
  const [downloading, setDownloading] = useState(false);

  const typeOptions = TYPE_OPTIONS.map(({ value, key }) => ({ value, label: t(key) }));
  const buildRequest = () => ({ config: config || {}, tables: selectedTables, types });

  const handleGenerate = () => {
    const tableTip =
      selectedTables.length > 0 ? selectedTables.join(', ') : t('generate.allTables');
    const typeTip = types.length === ALL_TYPES.length ? t('generate.allTypes') : types.join(', ');
    modal.confirm({
      title: t('generate.confirmTitle'),
      content: t('generate.confirmContent', { tables: tableTip, types: typeTip }),
      okText: t('generate.ok'),
      cancelText: t('common.cancel'),
      onOk: async () => {
        setGenerating(true);
        setResult(null);
        try {
          const res = await api.generate(config || {}, selectedTables, types);
          setResult({ success: res.success, message: res.message, files: res.files || [] });
          if (res.success) {
            message.success(res.message);
          } else {
            message.error(res.message || t('generate.failed'));
          }
        } catch (e) {
          setResult({ success: false, message: `${t('generate.failed')}: ${e.message}`, files: [] });
          message.error(`${t('generate.failed')}: ${e.message}`);
        } finally {
          setGenerating(false);
        }
      }
    });
  };

  const handlePreview = async () => {
    if (types.length === 0) {
      message.warning(t('generate.typeRequired'));
      return;
    }
    setPreviewing(true);
    setPreviewFiles([]);
    setPreviewIndex(0);
    setPreviewOpen(true);
    try {
      const res = await api.preview(config || {}, selectedTables, types);
      if (res.success) {
        const files = res.files || [];
        setPreviewFiles(files);
        if (files.length === 0) {
          message.warning(t('generate.noPreview'));
        }
      } else {
        message.error(res.message || t('generate.previewFailed'));
      }
    } catch (e) {
      message.error(`${t('generate.previewFailed')}: ${e.message}`);
    } finally {
      setPreviewing(false);
    }
  };

  const handleDownload = async () => {
    if (types.length === 0) {
      message.warning(t('generate.typeRequired'));
      return;
    }
    setDownloading(true);
    try {
      const blob = await api.download(config || {}, selectedTables, types);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'easy-codegen.zip';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      message.success(t('generate.downloadStarted'));
    } catch (e) {
      message.error(e.message || t('generate.downloadFailed'));
    } finally {
      setDownloading(false);
    }
  };

  const handleCopy = async () => {
    const file = previewFiles[previewIndex];
    if (!file) return;
    try {
      await navigator.clipboard.writeText(file.content);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (e) {
      message.error(t('generate.copyFailed'));
    }
  };

  const currentFile = previewFiles[previewIndex];

  return (
    <Card title={t('generate.title')}>
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        {/* 生成类型 */}
        <div>
          <div style={{ marginBottom: 8 }}>
            <Typography.Text strong>{t('generate.types')}</Typography.Text>
            <Typography.Text type="secondary" style={{ marginLeft: 8, fontSize: 12 }}>
              {t('generate.typesHint')}
            </Typography.Text>
          </div>
          <Checkbox.Group options={typeOptions} value={types} onChange={setTypes} />
        </div>

        <Divider style={{ margin: 0 }} />

        <Space wrap>
          <Button
            type="primary"
            size="large"
            icon={<RocketOutlined />}
            loading={generating}
            onClick={handleGenerate}
          >
            {t('generate.run')}
          </Button>
          <Button
            size="large"
            icon={<EyeOutlined />}
            loading={previewing}
            onClick={handlePreview}
          >
            {t('generate.preview')}
          </Button>
          <Button
            size="large"
            icon={<DownloadOutlined />}
            loading={downloading}
            onClick={handleDownload}
          >
            {t('generate.download')}
          </Button>
          <Typography.Text type="secondary">
            {t('generate.tablesLabel')}:{' '}
            {selectedTables.length > 0 ? selectedTables.join(', ') : t('generate.allTables')}
          </Typography.Text>
        </Space>

        {result && (
          <>
            <Alert
              type={result.success ? 'success' : 'error'}
              showIcon
              message={result.message}
              description={
                result.files.length > 0 && (
                  <List
                    size="small"
                    dataSource={result.files}
                    renderItem={(f) => (
                      <List.Item>
                        <Typography.Text style={{ fontFamily: 'Consolas, monospace', fontSize: 12 }}>
                          {f}
                        </Typography.Text>
                      </List.Item>
                    )}
                  />
                )
              }
            />
            <div>
              <Tag color={result.success ? 'green' : 'red'}>
                {t('generate.fileCount', { count: result.files.length })}
              </Tag>
            </div>
          </>
        )}
      </Space>

      {/* 代码预览抽屉 */}
      <Drawer
        title={t('generate.drawerTitle')}
        width={860}
        open={previewOpen}
        onClose={() => setPreviewOpen(false)}
        extra={
          <Tooltip title={t('generate.copyCurrent')}>
            <Button
              icon={copied ? <CheckOutlined /> : <CopyOutlined />}
              onClick={handleCopy}
              disabled={!currentFile}
            >
              {copied ? t('generate.copied') : t('generate.copy')}
            </Button>
          </Tooltip>
        }
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Select
            style={{ width: '100%' }}
            value={previewFiles[previewIndex] ? previewIndex : undefined}
            onChange={setPreviewIndex}
            placeholder={previewing ? t('generate.loadingPreview') : t('generate.selectFile')}
            options={previewFiles.map((f, i) => ({ value: i, label: f.name }))}
            showSearch
            optionFilterProp="label"
          />
          <div
            className="code-view"
            style={{
              borderRadius: 10,
              padding: 12,
              maxHeight: '62vh',
              overflow: 'auto'
            }}
          >
            {previewing && !currentFile && (
              <Typography.Text type="secondary">{t('generate.loadingPreview')}</Typography.Text>
            )}
            {!previewing && currentFile && (
              <pre style={{ margin: 0, fontFamily: 'Consolas, monospace', fontSize: 12, whiteSpace: 'pre', minWidth: 'max-content' }}>
                {currentFile.content}
              </pre>
            )}
            {!previewing && !currentFile && (
              <Typography.Text type="secondary">{t('generate.noFile')}</Typography.Text>
            )}
          </div>
        </Space>
      </Drawer>
    </Card>
  );
}
