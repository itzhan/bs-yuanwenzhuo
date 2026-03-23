declare namespace Api {
  namespace Auth {
    interface LoginToken {
      token: string;
      userId: number;
      username: string;
      realName: string;
      role: number;
    }

    interface UserInfo {
      userId: string;
      userName: string;
      roles: string[];
      buttons: string[];
    }
  }
}
