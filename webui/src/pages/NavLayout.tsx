import * as React from 'react'
import {
  Box,
  Grid,
  GridItem,
  ListItem,
  Link,
  UnorderedList,
} from '@chakra-ui/react'
import { Link as ReachLink } from '@reach/router'
import styled from 'styled-components'

import DeltaLogo from '../assets/delta_logo_simple.svg'
import ErrorBoundary from '../components/ErrorBoundary'

interface NavigationItemProps {
  title: string;
  to: string;
}

function NavigationItem(props: NavigationItemProps) {
  return (
    <ListItem>
      <Link
        as={ReachLink}
        to={props.to}
        fontSize="xl"
      >
        { props.title }
      </Link>
    </ListItem>
  )
}

function Navigation() {
  return (
    <React.Fragment>
      <Box mb="2em">
        <DeltaLogo width={50} height={50} />
      </Box>
      <nav>
        <UnorderedList
          styleType="none"
          ml={0}
          spacing={3}
        >
          <NavigationItem title="Dashboard" to="/" />
          <NavigationItem title="Players" to="/players" />
        </UnorderedList>
      </nav>
    </React.Fragment>
  )
}

type Props = {
  path: string;
  children: React.ReactChildren;
}

function NavLayout(props: Props) {
  return (
    <React.Fragment>
      <Grid
        h="100%"
        templateColumns="220px 1fr"
        templateRows="1fr"
      >
        <GridItem
          colSpan={1}
          py={5}
          px={5}
          bg="white"
          shadow="xl"
          border="1px solid"
          borderColor="gray.200"
        >
          <Navigation />
        </GridItem>
        <GridItem
          colStart={2} colEnd={3}
          p={3}
          bg="gray.100"
        >
          <ErrorBoundary>
            { props.children }
          </ErrorBoundary>
        </GridItem>
      </Grid>
    </React.Fragment>
  )
}

export default NavLayout
