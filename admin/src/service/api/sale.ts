import { request } from '../request';

export function fetchSaleList(params: {
  page: number;
  size: number;
  keyword?: string;
  drugId?: number;
}) {
  return request<any>({ url: '/api/sales', params });
}

export function fetchSaleDetail(id: number) {
  return request<any>({ url: `/api/sales/${id}` });
}

export function createSale(data: any) {
  return request<any>({ url: '/api/sales', method: 'post', data });
}

export function deleteSale(id: number) {
  return request<any>({ url: `/api/sales/${id}`, method: 'delete' });
}
