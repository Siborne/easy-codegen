import React, { useState } from 'react';
import { App, Card, Button, Table, Space, Tag } from 'antd';
import { SyncOutlined } from '@ant-design/icons';
import { api } from '../api';
import { useI18n } from '../i18n';

export default function TablePanel({ tables, selectedTables, onTablesLoaded, onSelectedChange }) {
  const { message } = App.useApp();
  const { t } = useI18n();
  const [loading, setLoading] = useState(false);

  const handleLoad = async () => {
    setLoading(true);
    try {
      const res = await api.listTables();
      if (res.success) {
        onTablesLoaded?.(res.tables || []);
        onSelectedChange?.([]);
        message.success(t('table.synced', { count: (res.tables || []).length }));
      } else {
        message.error(res.message || t('table.loadFailed'));
      }
    } catch (e) {
      message.error(`${t('table.loadError')}: ${e.message}`);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: t('table.colTable'), dataIndex: 'tableName', render: (v) => <Tag>{v}</Tag> },
    { title: t('table.colBean'), dataIndex: 'beanName', render: (v) => <Tag color="blue">{v}</Tag> },
    { title: t('table.colComment'), dataIndex: 'comment', ellipsis: true }
  ];

  const rowSelection = {
    selectedRowKeys: selectedTables,
    onChange: onSelectedChange
  };

  return (
    <Card
      title={t('table.title')}
      extra={
        <Space>
          <Button icon={<SyncOutlined />} loading={loading} onClick={handleLoad}>
            {t('table.sync')}
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
        locale={{ emptyText: t('table.empty') }}
      />
      <div style={{ marginTop: 8, color: '#888', fontSize: 13 }}>
        {t('table.summary', { total: tables.length, selected: selectedTables.length })}
      </div>
    </Card>
  );
}
