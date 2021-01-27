import * as React from 'react'
import { Box, Flex, Heading, Text } from '@chakra-ui/react'
import { gql, useQuery } from '@apollo/client'

import DataTable from '../components/DataTable'

const GET_PLAYERS = gql`
query PaginatedPlayers($offset: Int = 0, $limit: Int = 100) {
  players(offset: $offset, limit: $limit) {
    id
    name
    uuid
    lastOnlineTime
  }
}
`

const PLAYERS_COLUMNS = [
  {
    Header: 'id',
    accessor: 'id',
    width: 100,
  },
  {
    Header: 'name',
    accessor: 'name',
  },
  {
    Header: 'uuid',
    accessor: 'uuid',
  },
  {
    Header: 'Last Online Time',
    accessor: 'lastOnlineTime',
  },
]

function PlayersTable() {
  const { loading, error, data, fetchMore } = useQuery(GET_PLAYERS, {
    variables: { offset: 0, limit: 100 },
  })

  const loadMoreItems = React.useCallback((startIndex, stopIndex) => {
    return fetchMore({
      variables: {
        offset: data?.players[startIndex - 1].id,
        limit: stopIndex - startIndex + 1,
      }
    })
  }, [data])

  const isItemLoaded = React.useCallback((index) => {
    return Boolean(data?.players[index])
  }, [data])
  
  return (
    <DataTable
      loading={loading}
      columns={PLAYERS_COLUMNS}
      data={data?.players}
      isItemLoaded={isItemLoaded}
      loadMoreItems={loadMoreItems}
    />
  )
}

type Props = {
  path: string;
}


function PlayersPage(props: Props) {
  return (
    <Flex flexDir="column" h="100%">
      <Heading size="md" mb="4">Players</Heading>
      <Box flex="1">
        <PlayersTable />
      </Box>
    </Flex>
  )
}

export default PlayersPage
