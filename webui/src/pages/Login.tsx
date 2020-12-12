import * as React from 'react'
import {
  Button,
  FormControl,
  FormLabel,
  Input,
  Stack,
  Heading,
} from '@chakra-ui/react'
import jwtDecode from 'jwt-decode'
import { navigate } from '@reach/router'

import constants from '../constants'
import { fetchJson } from '../util'
import { useUserContext, UserInfo } from '../UserContext'

type Props = {
  path: string;
}

async function fetchAuthToken(username: string, password: string) {
  const tokenObj = await fetchJson('/auth', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  }, false)
  return tokenObj.token
}

function Login(props: Props) {
  const [username, setUsername] = React.useState<string>("");
  const [password, setPassword] = React.useState<string>("");
  const { setUserInfo } = useUserContext();
  const submitLogin = async (evt: React.FormEvent<HTMLFormElement>) => {
    evt.preventDefault()
    const token = await fetchAuthToken(username, password)
    localStorage.setItem("token", token)
    const jwt = jwtDecode<UserInfo>(token);
    setUserInfo(jwt)
    if (jwt.temporary) {
      navigate('/login/changepass')
    } else {
      navigate('/')
    }
    // .catch(err => console.log(err))
  }

  return (
    <React.Fragment>
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
              placeholder="password"
              isRequired
              size="lg"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </FormControl>
        </Stack>
        <Button colorScheme="blue" mt="2em" type="submit" w="100%">Log In</Button>
      </form>
    </React.Fragment>
  )
}

export default Login
