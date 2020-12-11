import * as React from 'react'
import constate from 'constate'

interface UserInfo {
  userId: string;
  userName: string;
}

function useUserInfo() {
  const [userInfo, setUserInfo] = React.useState<UserInfo>()
  return { userInfo, setUserInfo }
}

export const [UserContextProvider, useUserContext] = constate(useUserInfo)
