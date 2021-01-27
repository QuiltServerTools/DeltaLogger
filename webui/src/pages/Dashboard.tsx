import React from 'react'
import { Box, Flex, Heading } from '@chakra-ui/react'
import PlacementsTable from '../components/PresetTables/Placements'
import TransactionsTable from '../components/PresetTables/Transactions'

type Props = {
  path: string;
}

function Dashboard(props: Props) {
  return (
    <Flex flexDir="column" h="100%">
      <Heading size="md" py="4">Placements</Heading>
      <Box flex="1">
        <PlacementsTable />
      </Box>

      <Heading size="md" py="4">Transactions</Heading>
      <Box flex="1">
        <TransactionsTable />
      </Box>
    </Flex>
  )
}

export default Dashboard
