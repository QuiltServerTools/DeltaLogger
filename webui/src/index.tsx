import * as React from 'react'
import * as ReactDOM from 'react-dom'
import { ChakraProvider } from '@chakra-ui/react'
import { Router, Redirect, useLocation } from '@reach/router'
import { Helmet } from 'react-helmet'
import jwtDecode from 'jwt-decode'
import { ApolloClient, ApolloProvider, createHttpLink, InMemoryCache } from '@apollo/client'
import { setContext } from '@apollo/client/link/context'

import { UserContextProvider, UserInfo, useUserContext } from './UserContext'
import GlobalStyle from './GlobalStyle'
import Dashboard from './pages/Dashboard'
import Login from './pages/Login'
import constants from './constants'

import ChangePass from './pages/ChangePass'
import CenterFormLayout from './pages/CenterFormLayout'
import NavLayout from './pages/NavLayout'
import { isDevEnv } from './util'
import cacheSettings from './cacheSettings'

const httpLink = createHttpLink({
  uri: (isDevEnv() ? 'http://localhost:8080' :  '') + '/graphql',
});

const authLink = setContext((_, { headers }) => {
  // get the authentication token from local storage if it exists
  const token = localStorage.getItem('token');
  // return the headers to the context so httpLink can read them
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : "",
    }
  }
});

const client = new ApolloClient({
  link: authLink.concat(httpLink),
  cache: new InMemoryCache(cacheSettings)
});

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
      </ProtectedRoute>
      <CenterFormLayout path="/login">
        <ProtectedRoute component={ChangePass} path="/changepass" />
        <Login path="/" />
      </CenterFormLayout>
    </Router>
  )
}

function App() {
  return (
    <React.Fragment>
      <Helmet>
        <title>{constants.APP_NAME}</title>
      </Helmet>
      <GlobalStyle />
      <ApolloProvider client={client}>
        <UserContextProvider>
          <ChakraProvider>
            <Routes />
          </ChakraProvider>
        </UserContextProvider>
      </ApolloProvider>
    </React.Fragment>
  )
}

ReactDOM.render(
  <App />,
  document.getElementById('root'),
)
