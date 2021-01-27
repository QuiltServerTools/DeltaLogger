import * as React from 'react'
import { Box, chakra, Flex } from '@chakra-ui/react'
import styled from 'styled-components'
import { color, space, system } from 'styled-system'

const attachName = (displayName: string, fn: any) => {
  fn.displayName = displayName
  return fn
}

export const Table = attachName('Table', styled(Box).attrs(props => ({
  role: 'table',
}))`
  max-width: 100%;
  display: flex;
  flex-direction: column;
`)

export const Thead = attachName('Thead', styled(Box).attrs(props => ({
  role: 'rowgroup',
  borderColor: 'gray.200',
}))`
  border-bottom: 1px solid;
`)

export const Th = attachName('Th', styled(Box).attrs(props => ({
  role: 'columnheader',
  textAlign: 'left',
  px: '2',
  py: '2',
  fontWeight: '700',
  fontSize: '0.75rem',
  textTransform: 'uppercase',
  color: 'gray.500',
}))`
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
`)

export const Tr = attachName('Tr', styled(Box).attrs(props => ({
  role: 'row',
}))`
`)

export const Td = attachName('Td', styled(Box).attrs(props => ({
  role: 'cell',
  px: '2',
  fontSize: '0.85rem',
}))`
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
`)

export const Tbody = attachName('Tbody', styled(Box)``)
