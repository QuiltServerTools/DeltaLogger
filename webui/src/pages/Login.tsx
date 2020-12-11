import * as React from 'react'
import {
  Box,
  Button,
  Center,
  Container,
  Flex,
  FormControl,
  FormLabel,
  Input,
  Stack,
  Heading,
} from '@chakra-ui/react'

import constants from '../constants'
import { fetchJson } from '../util'

type Props = {
  path: string;
}

async function fetchAuthToken(username: string, password: string) {
  const tokenObj = await fetchJson('/auth', { method: 'POST', body: JSON.stringify({ username, password })})
  return tokenObj.token
}

function Login(props: Props) {
  const [username, setUsername] = React.useState<string>("");
  const [password, setPassword] = React.useState<string>("");
  const submitLogin = (evt: React.FormEvent<HTMLFormElement>) => {
    evt.preventDefault()
    fetchAuthToken(username, password)
      .then(token => console.log(token))
      .catch(err => console.log(err))
  }

  return (
    <Center h="100vh" bg="blue.50">
      <Container shadow="xl" padding="2em" rounded="md" bg="white">
        <Heading size="lg" mb="1em">{constants.APP_NAME}</Heading>
          <form onSubmit={submitLogin}>
            <Stack spacing={3}>
              <FormControl>
                <FormLabel>Minecraft User Name</FormLabel>
                <Input
                  placeholder="jeb_"
                  autoComplete="off" autoCorrect="off" autoCapitalize="off" spellCheck="false"
                  size="lg"
                  isRequired
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </FormControl>
              <FormControl>
                <FormLabel>{constants.APP_NAME} Password</FormLabel>
                <Input 
                  type="password"
                  isRequired
                  size="lg"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </FormControl>
            </Stack>
            <Button colorScheme="blue" mt="2em" type="submit" w="100%">Log In</Button>
          </form>
      </Container>
    </Center>
  )
}

export default Login
