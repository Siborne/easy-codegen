import React, { useState } from 'react';
import { App, Card, Button, Alert, List, Space, Tag, Typography } from 'antd';
import { RocketOutlined } from '@ant-design/icons';
import { api } from '../api';

export default function GeneratePanel({ config, selectedTables }) {
  const { message, modal } = App.useApp();
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState(null); // { success, message, files }

  const handleGenerate = () => {
    const tip =
      selectedTables.length > 0
        ? '将生成 ' + selectedTables.length + ' 张表: ' + selectedTables.join(', ')
        : '未勾选任何表，将生成全部表';
    modal.confirm({
      title: '确认生成？',
      content: tip,
      okText: '开始生成',
      cancelText: '取消',
      onOk: async () => {
        setGenerating(true);
        setResult(null);
        try {
          const res = await api.generate(config || {}, selectedTables);
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

  return (
    <Card title="代码生成">
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        <Space>
          <Button type="primary" size="large" icon={<RocketOutlined />} loading={generating} onClick={handleGenerate}>
            一键生成
          </Button>
          <Typography.Text type="secondary">
            当前选择: {selectedTables.length > 0 ? selectedTables.join(', ') : '全部表'}
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
                共生成 {result.files.length} 个文件
              </Tag>
            </div>
          </>
        )}
      </Space>
    </Card>
  );
}
