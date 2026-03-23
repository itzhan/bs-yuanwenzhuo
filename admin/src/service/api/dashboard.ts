import { request } from '../request';

export function fetchDashboardData() {
  return request<any>({ url: '/api/dashboard' });
}
