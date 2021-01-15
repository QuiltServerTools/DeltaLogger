import { Box } from '@chakra-ui/react'
import * as React from 'react'
import { Column, useBlockLayout, useFlexLayout, useResizeColumns, useTable } from 'react-table'
import { FixedSizeList } from 'react-window'
import InfiniteLoader from 'react-window-infinite-loader'
import AutoSizer from 'react-virtualized-auto-sizer'

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
  rowHeight?: number;
  /**
   * Whether to load more items when scroll reaches the end of the list
   */
  infinityLoading?: boolean;
  /**
   * react-window-infinite-loader' prop
   */
  loadMoreItems?: InfiniteLoader['props']['loadMoreItems'];
  /**
   * react-window-infinite-loader' prop
   */
  isItemLoaded?: InfiniteLoader['props']['isItemLoaded'];
}

function DataTable<D extends object>({
  loading,
  data = [],
  columns = [],
  infinityLoading = true,
  loadMoreItems = async () => null,
  isItemLoaded = () => true,
  rowHeight = 30,
}: DataTableProps<D>) {
  const defaultColumn = React.useMemo(() => ({
    minWidth: 30,
    width: 150,
    maxWidth: 400,
  }), [])

  const {
    getTableProps,
    getTableBodyProps,
    headerGroups,
    rows,
    prepareRow,
  } = useTable(
    {
      columns,
      data,
      defaultColumn,
    },
    useFlexLayout,
    // useResizeColumns,
  )

  const RenderRow = React.useCallback(({ index, style }) => {
    const row = rows[index];
    if (!row) return (
      <Tr>
        <Td>Loading...</Td>
      </Tr>
    )
    prepareRow(row)
    const sheight = `${rowHeight}px`;
    return (
      <Tr {...row.getRowProps({ style })}>
        {row.cells.map(cell => {
          return (
            <Td {...cell.getCellProps()} h={sheight} lineHeight={sheight}>
              {cell.render('Cell')}
            </Td>
          )
        })}
      </Tr>
    )
  }, [prepareRow, rows])

  if (loading) return (
    <Box m="4">
      loading...
    </Box>
  )

  const itemCount = infinityLoading ? rows.length + 100 : rows.length

  return (
    <Box
      rounded="xl"
      border="1px solid"
      borderColor="gray.200"
      bg="white"
      h="100%"
      display="block"
      p={4}
    >
      <Table {...getTableProps()} h="100%" w="100%">
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

        <Tbody {...getTableBodyProps()} flex={1}>
          <AutoSizer>
            {({ width, height }) => (
              <InfiniteLoader
                isItemLoaded={isItemLoaded}
                itemCount={itemCount}
                loadMoreItems={loadMoreItems}
                minimumBatchSize={100}
              >
                {({ onItemsRendered, ref }) => (
                  <FixedSizeList
                    ref={ref}
                    height={height}
                    width={width}
                    itemCount={itemCount}
                    itemSize={rowHeight}
                    onItemsRendered={onItemsRendered}
                  >
                    {RenderRow}
                  </FixedSizeList>
                )}
              </InfiniteLoader>
            )}
          </AutoSizer>
        </Tbody>
      </Table>
    </Box>
  )
}

export default DataTable
