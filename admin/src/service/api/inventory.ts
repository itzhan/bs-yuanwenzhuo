import { request } from '../request';

export function fetchInventoryList(params: {
  page: number;
  size: number;
  keyword?: string;
  drugId?: number;
}) {
  return request<any>({ url: '/api/inventory', params });
}

export function createInventory(data: any) {
  return request<any>({ url: '/api/inventory', method: 'post', data });
}

export function updateInventory(id: number, data: any) {
  return request<any>({ url: `/api/inventory/${id}`, method: 'put', data });
}

export function adjustStock(id: number, params: { quantity: number; reason: string }) {
  return request<any>({ url: `/api/inventory/${id}/adjust`, method: 'post', params });
}

export function fetchLowStock() {
  return request<any[]>({ url: '/api/inventory/low-stock' });
}

export function fetchExpiring(days?: number) {
  return request<any[]>({ url: '/api/inventory/expiring', params: { days } });
}

export function deleteInventory(id: number) {
  return request<any>({ url: `/api/inventory/${id}`, method: 'delete' });
}
