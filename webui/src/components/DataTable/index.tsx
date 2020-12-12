import { Box } from '@chakra-ui/react'
import * as React from 'react'
import { Column, useTable } from 'react-table'

import {
  Table,
  Thead,
  Tbody,
  Tr,
  Td,
  Th,
} from "./TableStyledComponents"

interface DataTableProps<D extends object> {
  data: D[];
  columns: Array<Column<D>>;
  loading: boolean;
}

function DataTable<D extends object>({
  loading,
  data = [],
  columns = [],
}: DataTableProps<D>) {
  const tableInstance = useTable({ columns, data })
  if (loading) return (
    <Box m="4">
      loading...
    </Box>
  )
 
  const {
    getTableProps,
    getTableBodyProps,
    headerGroups,
    rows,
    prepareRow,
  } = tableInstance

  return (
    <Box
      rounded="xl"
      border="1px solid"
      borderColor="gray.200"
      bg="white"
      p={4}
    >
      <Table {...getTableProps()}>
        <Thead>
          {headerGroups.map(headerGroup =>
            <Tr {...headerGroup.getHeaderGroupProps()}>
              {headerGroup.headers.map(column =>
                <Th {...column.getHeaderProps()}>
                  { column.render('Header') }
                </Th>
              )}
            </Tr>
          )}
        </Thead>
        <Tbody {...getTableBodyProps()}>
          {rows.map(row => {
            prepareRow(row)
            return (
              <Tr {...row.getRowProps()}>
                {row.cells.map(cell => {
                  return (
                    <Td {...cell.getCellProps()}>
                      {cell.render('Cell')}
                    </Td>
                  )
                })}
              </Tr>
            )
          })}
        </Tbody>
      </Table>
    </Box>
  )
}

export default DataTable
