import * as React from 'react'
import { Text } from '@chakra-ui/react'

import { gql, useQuery } from '@apollo/client'

import DataTable from '../DataTable'
import { trimMC } from '../../util'

const GET_KILLED_ENTITIES = gql`
query PaginatedKE($offset: Int = 0, $limit: Int = 100) {
  killedEntities(offset: $offset, limit: $limit) {
    id
    name
    time
    source
    killer
    dimension
    x
    y
    z
  }
}
`

const KILLED_ENTITIES_COLUMNS = [
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
    Header: 'killer',
    accessor: 'killer',
  },
  {
    Header: 'cause',
    accessor: 'source',
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
    accessor: (d: any) => d.dimension ? trimMC(d.dimension) : '',
  },
]

function KilledEntitiesTable() {
  const { loading, error, data, fetchMore } = useQuery(GET_KILLED_ENTITIES, {
    variables: { offset: 0, limit: 100 },
    pollInterval: 5000,
  })

  const loadMoreItems = React.useCallback((startIndex, stopIndex) => {
    return fetchMore({
      variables: {
        offset: data?.killedEntities[startIndex - 1].id,
        limit: stopIndex - startIndex + 1,
      }
    })
  }, [data])

  const isItemLoaded = React.useCallback((index) => {
    return Boolean(data?.killedEntities[index])
  }, [data])

  return (
    <DataTable
      loading={loading}
      columns={KILLED_ENTITIES_COLUMNS}
      data={data?.killedEntities}
      rowHeight={30}
      loadMoreItems={loadMoreItems}
      isItemLoaded={isItemLoaded}
    />
  )
}

export default KilledEntitiesTable
