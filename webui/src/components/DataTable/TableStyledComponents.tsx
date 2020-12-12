import * as React from 'react'
import { Box, chakra } from '@chakra-ui/react'
import styled from 'styled-components'
import { color, space, system } from 'styled-system'

export function Table(props: React.ComponentProps<typeof chakra.table>) {
  return (
    <chakra.table
      width="100%"
      {...props}
    />
  )
}

export function Thead(props: React.ComponentProps<typeof chakra.thead>) {
  return (
    <chakra.thead
      borderBottom="1px solid"
      borderColor="gray.200"
      {...props}
    />
  )
}

export function Th(props: React.ComponentProps<typeof chakra.th>) {
  return (
    <chakra.th
      textAlign="left"
      px="4"
      py="2"
      fontWeight="700"
      fontSize="0.75rem"
      textTransform="uppercase"
      color="gray.500"
      {...props}
    />
  )
}

export function Tr(props: React.ComponentProps<typeof chakra.tr>) {
  return (
    <chakra.tr
      {...props}
    />
  )
}

export function Td(props: React.ComponentProps<typeof chakra.td>) {
  return (
    <chakra.td
      px="4"
      py="2"
      fontSize="0.85rem"
      {...props}
    />
  )
}

export function Tbody(props: React.ComponentProps<typeof chakra.tbody>) {
  return (
    <chakra.tbody
      {...props}
    />
  )
}
