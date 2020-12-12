import * as React from 'react'
import {
  Center,
  Container,
} from '@chakra-ui/react'

type Props = {
  path: string;
  children: React.ReactNode;
}

function CenterFormLayout(props: Props) {
  return (
    <Center h="100vh" bg="blue.50">
      <Container shadow="xl" padding="2em" rounded="md" bg="white">
        { props.children }
      </Container>
    </Center>
  )
}

export default CenterFormLayout
