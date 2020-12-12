import * as React from 'react'
import {
  Button,
  FormControl,
  FormLabel,
  Input,
  Stack,
  Text,
  Heading,
} from '@chakra-ui/react'
import jwtDecode from 'jwt-decode'
import { navigate } from '@reach/router'


import CenterFormLayout from './CenterFormLayout'
import constants from '../constants'
import { fetchJson } from '../util'
import { UserInfo, useUserContext } from '../UserContext'

type Props = {
  path: string;
}

function ChangePass(props: Props) {
  const [password, setPassword] = React.useState<string>("")
  const { setUserInfo } = useUserContext();

  const submitChangePass = async (evt: React.FormEvent<HTMLFormElement>) => {
    evt.preventDefault()
    const { token } = await fetchJson('/auth/change-pass', {
      method: 'POST',
      body: JSON.stringify({ password })
    }, true)
    localStorage.setItem("token", token)
    const jwt = jwtDecode<UserInfo>(token);
    setUserInfo(jwt)
    navigate('/')
  }

  return (
    <React.Fragment>
      <Heading size="lg" mb="1em">Change Password</Heading>
      <Text mb="1em">
        Please change your password for {constants.APP_NAME}
      </Text>

      <form onSubmit={submitChangePass}>
        <FormControl>
          <FormLabel>New Password</FormLabel>
          <Input 
            type="password"
            placeholder="password"
            isRequired
            size="lg"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </FormControl>

        <Button colorScheme="blue" mt="2em" type="submit" w="100%">Submit</Button>
      </form>
    </React.Fragment>
  )
}

export default ChangePass
