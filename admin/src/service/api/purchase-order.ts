import { request } from '../request';

export function fetchPurchaseOrderList(params: {
  page: number;
  size: number;
  status?: number;
  keyword?: string;
}) {
  return request<any>({ url: '/api/purchase-orders', params });
}

export function fetchPurchaseOrderDetail(id: number) {
  return request<any>({ url: `/api/purchase-orders/${id}` });
}

export function createPurchaseOrder(data: any) {
  return request<any>({ url: '/api/purchase-orders', method: 'post', data });
}

export function approvePurchaseOrder(id: number) {
  return request<any>({ url: `/api/purchase-orders/${id}/approve`, method: 'put' });
}

export function receivePurchaseOrder(id: number) {
  return request<any>({ url: `/api/purchase-orders/${id}/receive`, method: 'put' });
}

export function cancelPurchaseOrder(id: number) {
  return request<any>({ url: `/api/purchase-orders/${id}/cancel`, method: 'put' });
}

export function deletePurchaseOrder(id: number) {
  return request<any>({ url: `/api/purchase-orders/${id}`, method: 'delete' });
}
