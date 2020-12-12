console.log(isDevEnv() ? 'dev env' : 'other env')

export function isDevEnv(): boolean {
  return process.env.NODE_ENV === 'development'
}

export function url(ending: string): string {
  return process.env.BASE_URL + ending;
}

export function fetchJson(_url: string, requestInit: RequestInit, useToken: boolean = true) {
  return fetch(url(_url), {
    ...requestInit,
    // ...(isDevEnv() ? { mode: 'no-cors' } : {}),
    headers: {
      ...requestInit.headers,
      ...(useToken ? { 'Authorization': localStorage.getItem('token') as string } : {}),
      'Content-Type': 'application/json'
    }
  }).then(r => r.json())
}