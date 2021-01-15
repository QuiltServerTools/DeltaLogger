import React from 'react'
import { Router, Redirect, useLocation } from '@reach/router'
import jwtDecode from 'jwt-decode'

import { UserInfo, useUserContext } from './UserContext'
import Dashboard from './pages/Dashboard'
import Players from './pages/Players'
import Login from './pages/Login'

import ChangePass from './pages/ChangePass'
import CenterFormLayout from './pages/CenterFormLayout'
import NavLayout from './pages/NavLayout'

interface ProtectedRouteProps {
  component: React.JSXElementConstructor<any>;
  children?: React.ReactNode;
  path?: string;
}

function ProtectedRoute({ component: Component, ...rest }: ProtectedRouteProps) {
  const location = useLocation()
  const { userInfo } = useUserContext()

  return userInfo && (!userInfo.temporary || location.pathname === '/login/changepass')
    ? <Component {...rest} />
    : userInfo?.temporary
      ? <Redirect from="" to="/login/changepass" noThrow />
      : <Redirect from="" to="/login" noThrow />
}



function Routes() {
  const { userInfo, setUserInfo } = useUserContext()

  // Restore token from local storage
  const token = localStorage.getItem('token')
  React.useEffect(() => {
    if (token) {
      const jwt = jwtDecode<UserInfo>(token)
      setUserInfo(jwt);
    }
  }, [token])

  return token && !userInfo ? null : (
    <Router>
      <ProtectedRoute component={NavLayout} path="/">
        <Dashboard path="/" />
        <Players path="/players" />
      </ProtectedRoute>
      <CenterFormLayout path="/login">
        <ProtectedRoute component={ChangePass} path="/changepass" />
        <Login path="/" />
      </CenterFormLayout>
    </Router>
  )
}

export default Routes
