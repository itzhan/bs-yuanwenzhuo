import { request } from '../request';

export function fetchUserList(params: {
  page: number;
  size: number;
  keyword?: string;
}) {
  return request<any>({ url: '/api/users', params });
}

export function fetchAllUsers() {
  return request<any[]>({ url: '/api/users/all' });
}

export function fetchUserDetail(id: number) {
  return request<any>({ url: `/api/users/${id}` });
}

export function createUser(data: any) {
  return request<any>({ url: '/api/users', method: 'post', data });
}

export function updateUser(id: number, data: any) {
  return request<any>({ url: `/api/users/${id}`, method: 'put', data });
}

export function deleteUser(id: number) {
  return request<any>({ url: `/api/users/${id}`, method: 'delete' });
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request<any>({ url: '/api/users/change-password', method: 'post', data });
}
