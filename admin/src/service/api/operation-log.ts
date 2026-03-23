import { request } from '../request';

export function fetchOperationLogList(params: {
  page: number;
  size: number;
  module?: string;
  username?: string;
}) {
  return request<any>({ url: '/api/operation-logs', params });
}
