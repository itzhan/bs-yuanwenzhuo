import { request } from '../request';

export function fetchCategoryList() {
  return request<any[]>({ url: '/api/categories' });
}

export function fetchCategoryTree() {
  return request<any[]>({ url: '/api/categories/tree' });
}

export function createCategory(data: any) {
  return request<any>({ url: '/api/categories', method: 'post', data });
}

export function updateCategory(id: number, data: any) {
  return request<any>({ url: `/api/categories/${id}`, method: 'put', data });
}

export function deleteCategory(id: number) {
  return request<any>({ url: `/api/categories/${id}`, method: 'delete' });
}
