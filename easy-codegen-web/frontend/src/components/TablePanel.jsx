import React, { useState } from 'react';
import { App, Card, Button, Table, Space, Tag } from 'antd';
import { TableOutlined } from '@ant-design/icons';
import { api } from '../api';

export default function TablePanel({ tables, selectedTables, onTablesLoaded, onSelectedChange }) {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);

  const handleLoad = async () => {
    setLoading(true);
    try {
      const res = await api.listTables();
      if (res.success) {
        onTablesLoaded?.(res.tables || []);
        onSelectedChange?.([]);
        message.success('已加载 ' + (res.tables || []).length + ' 张表');
      } else {
        message.error(res.message || '加载表失败');
      }
    } catch (e) {
      message.error('加载失败: ' + e.message);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: '表名', dataIndex: 'tableName', render: (v) => <Tag>{v}</Tag> },
    { title: 'Bean 名', dataIndex: 'beanName', render: (v) => <Tag color="blue">{v}</Tag> },
    { title: '注释', dataIndex: 'comment', ellipsis: true }
  ];

  const rowSelection = {
    selectedRowKeys: selectedTables,
    onChange: onSelectedChange
  };

  return (
    <Card
      title="数据表"
      extra={
        <Space>
          <Button icon={<TableOutlined />} loading={loading} onClick={handleLoad}>
            加载表列表
          </Button>
        </Space>
      }
    >
      <Table
        rowKey="tableName"
        size="middle"
        loading={loading}
        dataSource={tables}
        columns={columns}
        rowSelection={rowSelection}
        pagination={tables.length > 10 ? { pageSize: 10, showSizeChanger: false } : false}
        locale={{ emptyText: '点击右上角"加载表列表"获取当前数据库的表' }}
      />
      <div style={{ marginTop: 8, color: '#888', fontSize: 13 }}>
        共 {tables.length} 张表，已选 {selectedTables.length} 张；未勾选任何表时生成全部表
      </div>
    </Card>
  );
}
