import { request } from '../request';

export function fetchSupplierList(params: {
  page: number;
  size: number;
  keyword?: string;
}) {
  return request<any>({ url: '/api/suppliers', params });
}

export function fetchAllSuppliers() {
  return request<any[]>({ url: '/api/suppliers/all' });
}

export function createSupplier(data: any) {
  return request<any>({ url: '/api/suppliers', method: 'post', data });
}

export function updateSupplier(id: number, data: any) {
  return request<any>({ url: `/api/suppliers/${id}`, method: 'put', data });
}

export function deleteSupplier(id: number) {
  return request<any>({ url: `/api/suppliers/${id}`, method: 'delete' });
}
