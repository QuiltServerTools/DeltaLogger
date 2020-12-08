import React from 'react'
import ReactDOM from 'react-dom'
import { ChakraProvider } from '@chakra-ui/react'
import { Router } from '@reach/router'
import { Helmet } from 'react-helmet'

import GlobalStyle from './GlobalStyle'
import Dashboard from './pages/Dashboard'
import constants from './constants'

function Routes() {
  return (
    <Router>
      <Dashboard path="/" />
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
      <ChakraProvider>
        <Routes />
      </ChakraProvider>
    </React.Fragment>
  )
}

ReactDOM.render(
  <App />,
  document.getElementById('root'),
)
