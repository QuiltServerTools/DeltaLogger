import React from 'react'
import { Box, Flex, Heading, VStack, Text } from '@chakra-ui/react'
import MobGriefTable from '../components/PresetTables/MobGrief'

type Props = {
  path: string;
}

function MobGrief(props: Props) {
  return (
    <Flex flexDir="column" h="100%">
      <VStack mb="4" align="left">
        <Heading size="md">Mob Grief</Heading>
        <Text>Grief caused by entity explosions and their target player</Text>
      </VStack>
      <Box flex="1">
        <MobGriefTable />
      </Box>
    </Flex>
  )
}

export default MobGrief