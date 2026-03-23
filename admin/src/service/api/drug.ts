import { request } from '../request';

export function fetchDrugList(params: {
  page: number;
  size: number;
  keyword?: string;
  categoryId?: number;
  status?: number;
}) {
  return request<any>({ url: '/api/drugs', params });
}

export function fetchDrugDetail(id: number) {
  return request<any>({ url: `/api/drugs/${id}` });
}

export function fetchAllDrugs() {
  return request<any[]>({ url: '/api/drugs/all' });
}

export function createDrug(data: any) {
  return request<any>({ url: '/api/drugs', method: 'post', data });
}

export function updateDrug(id: number, data: any) {
  return request<any>({ url: `/api/drugs/${id}`, method: 'put', data });
}

export function deleteDrug(id: number) {
  return request<any>({ url: `/api/drugs/${id}`, method: 'delete' });
}
