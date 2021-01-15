import * as React from 'react'
import { Heading, Text } from '@chakra-ui/react'
import { gql, useQuery } from '@apollo/client'

import DataTable from '../components/DataTable'

type Props = {
  path: string;
}

const GET_PLACEMENTS = gql`
query PaginatedPlacements($offset: Int = 0, $limit: Int = 50) {
  placements(offset: $offset, limit: $limit) {
    id
    playerName
    blockType
    placed
    x
    y
    z
    time
    dimension
  }
}
`

const PLACEMENT_COLUMNS = [
  {
    Header: 'id',
    accessor: 'id',
    width: 100,
  },
  {
    Header: 'time',
    accessor: 'time',
  },
  {
    Header: 'player',
    accessor: 'playerName',
  },
  {
    Header: 'placed',
    accessor: (d: any) => d.placed ? (
      <Text color="green.400" fontWeight="700">placed</Text>
    ) : (
      <Text color="red.400" fontWeight="700">removed</Text>
    ),
  },
  {
    Header: 'block type',
    accessor: 'blockType',
  },
  {
    Header: '(x,y,z)',
    accessor: (d: any) => (
      <Text>
        {`(${d.x}, ${d.y}, ${d.z})`}
      </Text>
    ),
  },
  {
    Header: 'dimension',
    accessor: 'dimension',
  },
]

function PlacementsTable() {
  const { loading, error, data, fetchMore } = useQuery(GET_PLACEMENTS, {
    variables: { offset: 0, limit: 100 },
  })

  const loadMoreItems = React.useCallback((startIndex, stopIndex) => {
    return fetchMore({
      variables: {
        offset: data?.placements[startIndex - 1].id,
        limit: stopIndex - startIndex + 1,
      }
    })
  }, [data])

  const isItemLoaded = React.useCallback((index) => {
    return Boolean(data?.placements[index])
  }, [data])

  if (error) console.error(error.message)

  return (
    <DataTable
      loading={loading}
      columns={PLACEMENT_COLUMNS}
      data={data?.placements}
      rowHeight={30}
      loadMoreItems={loadMoreItems}
      isItemLoaded={isItemLoaded}
    />
  )
}

const GET_PLAYERS = () => gql`
query {
  players {
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
    accessor: 'id'
  },
  {
    Header: 'name',
    accessor: 'name'
  },
  {
    Header: 'uuid',
    accessor: 'uuid'
  },
  {
    Header: 'Last Online Time',
    accessor: 'lastOnlineTime'
  },
]

// function PlayersTable() {
//   const { loading, error, data } = useQuery(GET_PLAYERS())
//   return (
//     <DataTable
//       loading={loading}
//       columns={PLAYERS_COLUMNS}
//       data={data?.players}
//     />
//   )
// }

function Dashboard(props: Props) {
  return (
    <React.Fragment>
      <PlacementsTable />
      {/* <PlayersTable /> */}
    </React.Fragment>
  )
}

export default Dashboard
