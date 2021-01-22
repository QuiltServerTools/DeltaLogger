import * as React from 'react'
import {
  Box,
  Grid,
  GridItem,
  Icon,
  ListItem,
  Link,
  UnorderedList,
  Flex,
  HStack,
} from '@chakra-ui/react'
import { Link as ReachLink } from '@reach/router'
import styled from 'styled-components'
import { HiLogout } from 'react-icons/hi'
import { FaDiscord, FaGithubAlt } from 'react-icons/fa'

import DeltaLogo from '../assets/delta_logo_simple.svg'
import ErrorBoundary from '../components/ErrorBoundary'
import { IconType } from 'react-icons'

interface NavigationItemProps extends React.ComponentProps<typeof Link> {
  title: string;
  to: string;
  icon?: React.ReactNode;
}

function NavigationItem({ to, title, isExternal, icon, ...rest }: NavigationItemProps) {
  return (
    <ListItem>
      <Link
        as={!isExternal ? ReachLink : undefined}
        isExternal={isExternal}
        href={isExternal ? to : undefined}
        to={to}
        fontSize="xl"
        textAlign="center"
        {...rest}
      >
        {icon ? icon : null }
        { title }
      </Link>
    </ListItem>
  )
}

function Navigation() {
  return (
    <Flex flexDir="column" h="100%">
      <Box mb="2em">
        <DeltaLogo width={50} height={50} />
      </Box>
      <Flex flexDir="column" flex={1}>
        <Box as="nav" flex={1}>
          <UnorderedList
            styleType="none"
            ml={0}
            spacing={3}
          >
            <NavigationItem title="Dashboard" to="/" />
            <NavigationItem title="Players" to="/players" />
            <NavigationItem title="Killed Entities" to="/killed-entities" />
          </UnorderedList>
        </Box>
        <Box>
          <UnorderedList
            styleType="none"
            ml={0}
            spacing={3}
            color="gray.600"
          >
            <NavigationItem
              title="Github" to="https://github.com/fabricservertools/DeltaLogger"
              isExternal
              fontSize="md"
              icon={<Icon as={FaDiscord} mb="3px" mr="1" />}
            />
            <NavigationItem
              title="Discord" to="https://discord.gg/UEAZsRdxe2"
              isExternal
              fontSize="md"
              icon={<Icon as={FaGithubAlt} mb="3px" mr="1" />}
            />
            <NavigationItem
              title="Logout" to="/logout"
              fontSize="md"
              icon={<Icon as={HiLogout} mb="3px" mr="1" />}
            />
          </UnorderedList>
        </Box>
      </Flex>

    </Flex>
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
