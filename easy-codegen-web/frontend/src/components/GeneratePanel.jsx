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

const TYPE_OPTIONS = [
  { value: 'base', label: '基础类' },
  { value: 'po', label: 'PO 实体' },
  { value: 'query', label: 'Query 查询' },
  { value: 'mapper', label: 'Mapper 接口' },
  { value: 'mapperXml', label: 'Mapper XML' },
  { value: 'service', label: 'Service 接口' },
  { value: 'serviceImpl', label: 'ServiceImpl' },
  { value: 'controller', label: 'Controller' }
];
const ALL_TYPES = TYPE_OPTIONS.map((t) => t.value);

export default function GeneratePanel({ config, selectedTables }) {
  const { message, modal } = App.useApp();
  const [types, setTypes] = useState(ALL_TYPES);
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState(null); // { success, message, files }
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewing, setPreviewing] = useState(false);
  const [previewFiles, setPreviewFiles] = useState([]);
  const [previewIndex, setPreviewIndex] = useState(0);
  const [copied, setCopied] = useState(false);
  const [downloading, setDownloading] = useState(false);

  const buildRequest = () => ({ config: config || {}, tables: selectedTables, types });

  const handleGenerate = () => {
    const tableTip =
      selectedTables.length > 0 ? selectedTables.join(', ') : '全部表';
    const typeTip = types.length === ALL_TYPES.length ? '全部类型' : types.join(', ');
    modal.confirm({
      title: '确认生成？',
      content: '表: ' + tableTip + '；类型: ' + typeTip,
      okText: '开始生成',
      cancelText: '取消',
      onOk: async () => {
        setGenerating(true);
        setResult(null);
        try {
          const res = await api.generate(config || {}, selectedTables, types);
          setResult({ success: res.success, message: res.message, files: res.files || [] });
          if (res.success) {
            message.success(res.message);
          } else {
            message.error(res.message || '生成失败');
          }
        } catch (e) {
          setResult({ success: false, message: '生成失败: ' + e.message, files: [] });
          message.error('生成失败: ' + e.message);
        } finally {
          setGenerating(false);
        }
      }
    });
  };

  const handlePreview = async () => {
    if (types.length === 0) {
      message.warning('请至少选择一种生成类型');
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
          message.warning('没有可预览的文件，请检查表与类型选择');
        }
      } else {
        message.error(res.message || '预览失败');
      }
    } catch (e) {
      message.error('预览失败: ' + e.message);
    } finally {
      setPreviewing(false);
    }
  };

  const handleDownload = async () => {
    if (types.length === 0) {
      message.warning('请至少选择一种生成类型');
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
      message.success('已开始下载 easy-codegen.zip');
    } catch (e) {
      message.error(e.message || '下载失败');
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
      message.error('复制失败');
    }
  };

  const currentFile = previewFiles[previewIndex];

  return (
    <Card title="代码生成">
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        {/* 生成类型 */}
        <div>
          <div style={{ marginBottom: 8 }}>
            <Typography.Text strong>生成类型</Typography.Text>
            <Typography.Text type="secondary" style={{ marginLeft: 8, fontSize: 12 }}>
              可只生成需要的类型，如仅 Entity 或仅 Mapper
            </Typography.Text>
          </div>
          <Checkbox.Group
            options={TYPE_OPTIONS}
            value={types}
            onChange={setTypes}
          />
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
            一键生成
          </Button>
          <Button
            size="large"
            icon={<EyeOutlined />}
            loading={previewing}
            onClick={handlePreview}
          >
            代码预览
          </Button>
          <Button
            size="large"
            icon={<DownloadOutlined />}
            loading={downloading}
            onClick={handleDownload}
          >
            下载 ZIP
          </Button>
          <Typography.Text type="secondary">
            表: {selectedTables.length > 0 ? selectedTables.join(', ') : '全部表'}
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
              <Tag color={result.success ? 'green' : 'red'}>共生成 {result.files.length} 个文件</Tag>
            </div>
          </>
        )}
      </Space>

      {/* 代码预览抽屉 */}
      <Drawer
        title="代码预览"
        width={860}
        open={previewOpen}
        onClose={() => setPreviewOpen(false)}
        extra={
          <Tooltip title="复制当前文件">
            <Button
              icon={copied ? <CheckOutlined /> : <CopyOutlined />}
              onClick={handleCopy}
              disabled={!currentFile}
            >
              {copied ? '已复制' : '复制'}
            </Button>
          </Tooltip>
        }
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Select
            style={{ width: '100%' }}
            value={previewFiles[previewIndex] ? previewIndex : undefined}
            onChange={setPreviewIndex}
            placeholder={previewing ? '正在生成预览...' : '选择文件'}
            options={previewFiles.map((f, i) => ({ value: i, label: f.name }))}
            showSearch
            optionFilterProp="label"
          />
          <div
            style={{
              background: 'rgba(0,0,0,0.04)',
              borderRadius: 6,
              padding: 12,
              maxHeight: '62vh',
              overflow: 'auto'
            }}
          >
            {previewing && !currentFile && <Typography.Text type="secondary">正在生成预览...</Typography.Text>}
            {!previewing && currentFile && (
              <pre style={{ margin: 0, fontFamily: 'Consolas, monospace', fontSize: 12, whiteSpace: 'pre', minWidth: 'max-content' }}>
                {currentFile.content}
              </pre>
            )}
            {!previewing && !currentFile && (
              <Typography.Text type="secondary">无文件，请检查表与类型选择</Typography.Text>
            )}
          </div>
        </Space>
      </Drawer>
    </Card>
  );
}
