import * as React from 'react'
import { Heading, Text } from '@chakra-ui/react'
import { gql, useQuery } from '@apollo/client'

import DataTable from '../components/DataTable'

type Props = {
  path: string;
}

const GET_PLACEMENTS = () => gql`
  {
    placements {
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
  const { loading, error, data } = useQuery(GET_PLACEMENTS())
  return (
    <DataTable
      loading={loading}
      columns={PLACEMENT_COLUMNS}
      data={data?.placements}
    />
  )
}

function Dashboard(props: Props) {
  return (
    <React.Fragment>
      <PlacementsTable />
    </React.Fragment>
  )
}

export default Dashboard
