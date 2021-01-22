import React from 'react'
import { Box, Flex, Heading, VStack, Text } from '@chakra-ui/react'
import KilledEntitiesTable from '../components/PresetTables/KilledEntities'

type Props = {
  path: string;
}

function KilledEntities(props: Props) {
  return (
    <Flex flexDir="column" h="100%">
      <VStack mb="4" align="left">

      <Heading size="md">Killed Entities</Heading>
      <Text>Logged name-tagged-entity deaths</Text>
      </VStack>
      <Box flex="1">
        <KilledEntitiesTable />
      </Box>
    </Flex>
  )
}

export default KilledEntities