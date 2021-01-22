console.log(isDevEnv() ? 'dev env' : 'other env')

export function isDevEnv(): boolean {
  return process.env.NODE_ENV === 'development'
}

export function url(ending: string): string {
  return process.env.BASE_URL + ending;
}

export class CredentialError extends Error {
  constructor(msg: string) {
    super(msg)
    this.name = 'CredentialError'
  }
}

export async function fetchJson(_url: string, requestInit: RequestInit, useToken: boolean = true) {
  const r = await fetch(url(_url), {
    ...requestInit,
    // ...(isDevEnv() ? { mode: 'no-cors' } : {}),
    headers: {
      ...requestInit.headers,
      ...(useToken ? { 'Authorization': (localStorage.getItem('token') as string) } : {}),
      'Content-Type': 'application/json'
    }
  })
  if (r.status === 401) throw new CredentialError('Invalid credentials')
  return await r.json()
}

export function trimMC(s: string) {
  return s.replace(/^minecraft:/, '')
}
