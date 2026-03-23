import { request } from '../request';

export function fetchInventoryLogList(params: {
  page: number;
  size: number;
  drugId?: number;
  type?: number;
}) {
  return request<any>({ url: '/api/inventory-logs', params });
}

export function createInventoryLog(data: any) {
  return request<any>({ url: '/api/inventory-logs', method: 'post', data });
}

export function deleteInventoryLog(id: number) {
  return request<any>({ url: `/api/inventory-logs/${id}`, method: 'delete' });
}
