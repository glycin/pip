import type {PipRequestBody, PipResponse} from './types.js'

const BASE_URL = process.env.PIP_SERVER_URL ?? 'http://localhost:1337'

export async function chat(input: string, chatId: string): Promise<string> {
  const body: PipRequestBody = {
    input,
    think: false,
    chatId,
    category: 'JUST_CHATTING',
    categoryReason: 'tui demo',
  }
  const res = await fetch(`${BASE_URL}/pip/help`, {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      accept: 'application/json',
    },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    throw new Error(`pip-server responded ${res.status}`)
  }
  const data = (await res.json()) as PipResponse
  return data.response
}
