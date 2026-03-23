import { request } from '../request';

export function fetchExpiryAlertList(params: {
  page: number;
  size: number;
  alertLevel?: number;
  status?: number;
}) {
  return request<any>({ url: '/api/expiry-alerts', params });
}

export function handleExpiryAlert(id: number, data: { handlerNote: string }) {
  return request<any>({ url: `/api/expiry-alerts/${id}/handle`, method: 'put', data });
}

export function generateExpiryAlerts() {
  return request<any>({ url: '/api/expiry-alerts/generate', method: 'post' });
}

export function fetchPendingAlertCount() {
  return request<number>({ url: '/api/expiry-alerts/pending-count' });
}

export function deleteExpiryAlert(id: number) {
  return request<any>({ url: `/api/expiry-alerts/${id}`, method: 'delete' });
}
