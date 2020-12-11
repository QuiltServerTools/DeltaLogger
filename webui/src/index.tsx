import * as React from 'react'
import * as ReactDOM from 'react-dom'
import { ChakraProvider } from '@chakra-ui/react'
import { Router, Redirect } from '@reach/router'
import { Helmet } from 'react-helmet'

import { UserContextProvider, useUserContext } from './UserContext'
import GlobalStyle from './GlobalStyle'
import Dashboard from './pages/Dashboard'
import Login from './pages/Login'
import constants from './constants'

import { ApolloClient, InMemoryCache } from '@apollo/client';

// const client = new ApolloClient({
//   uri: 'https://48p1r2roz4.sse.codesandbox.io',
//   cache: new InMemoryCache()
// });

interface ProtectedRouteProps {
  component: React.JSXElementConstructor<any>;
  path?: string;
}

function ProtectedRoute({ component: Component, ...rest }: ProtectedRouteProps) {
  const { userInfo } = useUserContext()
  return Boolean(userInfo)
    ? <Component {...rest} />
    : <Redirect from="" to="login" noThrow />
}

function Routes() {
  return (
    <Router>
      <ProtectedRoute component={Dashboard} path="/" />
      <Login path="/login" />
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
      <UserContextProvider>
        <ChakraProvider>
          <Routes />
        </ChakraProvider>
      </UserContextProvider>
    </React.Fragment>
  )
}

ReactDOM.render(
  <App />,
  document.getElementById('root'),
)
