import React from 'react'
import { navigate } from '@reach/router'

import { useUserContext } from '../UserContext'

type Props = {
  path: string;
}

function Logout(props: Props) {
  const { setUserInfo } = useUserContext();

  React.useEffect(() => {
    setUserInfo(undefined)
    localStorage.removeItem('token')
    navigate('/login')
  })

  return null
}

export default Logout
