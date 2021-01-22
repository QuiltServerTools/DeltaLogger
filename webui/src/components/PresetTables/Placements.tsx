import * as React from 'react'
import { Text } from '@chakra-ui/react'

import { gql, useQuery } from '@apollo/client'

import DataTable from '../DataTable'
import { trimMC } from '../../util'

const GET_PLACEMENTS = gql`
query PaginatedPlacements($offset: Int = 0, $limit: Int = 100) {
  placements(offset: $offset, limit: $limit) {
    id
    playerName
    blockType
    state
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
    accessor: (d: any) => trimMC(d.blockType),
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
    accessor: (d: any) => trimMC(d.dimension),
  },
  {
    Header: 'block state',
    accessor: 'state',
  },
]

function PlacementsTable() {
  const { loading, error, data, fetchMore } = useQuery(GET_PLACEMENTS, {
    variables: { offset: 0, limit: 100 },
    pollInterval: 5000,
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

export default PlacementsTable
