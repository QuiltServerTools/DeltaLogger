import * as React from 'react'
import * as ReactDOM from 'react-dom'
import { ChakraProvider } from '@chakra-ui/react'
import { Helmet } from 'react-helmet'
import { ApolloClient, ApolloProvider, createHttpLink, InMemoryCache } from '@apollo/client'
import { setContext } from '@apollo/client/link/context'

import { UserContextProvider } from './UserContext'
import GlobalStyle from './GlobalStyle'
import constants from './constants'
import Routes from './Routes'

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
