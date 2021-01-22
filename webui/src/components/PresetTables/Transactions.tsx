import * as React from 'react'
import { Text } from '@chakra-ui/react'

import { gql, useQuery } from '@apollo/client'

import DataTable from '../DataTable'
import { trimMC } from '../../util'

const GET_TRANSACTIONS = gql`
query PaginatedTransactions($offset: Int = 0, $limit: Int = 100) {
  transactions(offset: $offset, limit: $limit) {
    id
    time
    playerName
    itemType
    count
    containerUUID
  }
}
`

const TRANSACTION_COLUMNS = [
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
    Header: 'item type',
    accessor: (d: any) => trimMC(d.itemType),
  },
  {
    Header: 'count',
    accessor: 'count',
  },
  {
    Header: 'container id',
    accessor: 'containerUUID',
  },
]

function TransactionsTable() {
  const { loading, error, data, fetchMore } = useQuery(GET_TRANSACTIONS, {
    variables: { offset: 0, limit: 100 },
    pollInterval: 5000,
  })

  const loadMoreItems = React.useCallback((startIndex, stopIndex) => {
    return fetchMore({
      variables: {
        offset: data?.transactions[startIndex - 1].id,
        limit: stopIndex - startIndex + 1,
      }
    })
  }, [data])

  const isItemLoaded = React.useCallback((index) => {
    return Boolean(data?.transactions[index])
  }, [data])

  return (
    <DataTable
      loading={loading}
      columns={TRANSACTION_COLUMNS}
      data={data?.transactions}
      rowHeight={30}
      loadMoreItems={loadMoreItems}
      isItemLoaded={isItemLoaded}
    />
  )
}

export default TransactionsTable
