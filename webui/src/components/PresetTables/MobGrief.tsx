import * as React from 'react'
import { Text } from '@chakra-ui/react'

import { gql, useQuery } from '@apollo/client'

import DataTable from '../DataTable'
import { trimMC } from '../../util'

const GET_MOB_GRIEF = gql`
query PaginatedKE($offset: Int = 0, $limit: Int = 100) {
  mobGrief(offset: $offset, limit: $limit) {
    id
    time
    entityType
    target
    dimension
    x
    y
    z
  }
}
`

const MOB_GRIEF_COLUMNS = [
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
    Header: 'target',
    accessor: 'target',
  },
  {
    Header: 'entity',
    accessor: 'entityType',
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
  const { loading, error, data, fetchMore } = useQuery(GET_MOB_GRIEF, {
    variables: { offset: 0, limit: 100 },
    pollInterval: 5000,
  })

  const loadMoreItems = React.useCallback((startIndex, stopIndex) => {
    return fetchMore({
      variables: {
        offset: data?.mobGrief[startIndex - 1].id,
        limit: stopIndex - startIndex + 1,
      }
    })
  }, [data])

  const isItemLoaded = React.useCallback((index) => {
    return Boolean(data?.mobGrief[index])
  }, [data])

  return (
    <DataTable
      loading={loading}
      columns={MOB_GRIEF_COLUMNS}
      data={data?.mobGrief}
      rowHeight={30}
      loadMoreItems={loadMoreItems}
      isItemLoaded={isItemLoaded}
    />
  )
}

export default KilledEntitiesTable
