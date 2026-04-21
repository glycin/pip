import type {CategorizationDto, PipRequestBody, PipResponse} from './types.js'

const BASE_URL = process.env.PIP_SERVER_URL ?? 'http://localhost:1337'

async function postJson<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      accept: 'application/json',
    },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    throw new Error(`pip-server ${path} responded ${res.status}`)
  }
  return (await res.json()) as T
}

export async function help(input: string, chatId: string): Promise<PipResponse> {
  const base: PipRequestBody = {input, think: false, chatId}
  const cat = await postJson<CategorizationDto>('/pip/categorize', base)
  return postJson<PipResponse>('/pip/help', {
    ...base,
    category: cat.category,
    categoryReason: cat.reason,
  })
}
