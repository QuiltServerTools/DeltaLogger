import * as React from 'react'
import constate from 'constate'

export interface UserInfo {
  iat: number;
  temporary: boolean;
  user_name: string;
  user_id: string;
}

function useUserInfo() {
  const [userInfo, setUserInfo] = React.useState<UserInfo>()
  return { userInfo, setUserInfo }
}

export const [UserContextProvider, useUserContext] = constate(useUserInfo)
