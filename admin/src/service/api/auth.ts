import { request } from '../request';

export function fetchLogin(userName: string, password: string) {
  return request<Api.Auth.LoginToken>({
    url: '/api/auth/login',
    method: 'post',
    data: {
      username: userName,
      password
    }
  });
}

export function fetchGetUserInfo() {
  return request<Api.Auth.UserInfo>({ url: '/api/auth/info' });
}

export function fetchRefreshToken(_refreshToken: string) {
  return request<Api.Auth.LoginToken>({
    url: '/api/auth/login',
    method: 'post',
    data: { username: '', password: '' }
  });
}
