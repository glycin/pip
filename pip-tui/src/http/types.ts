export interface PipRequestBody {
  input: string
  think: boolean
  chatId: string
  category?: string
  categoryReason?: string
}

export interface PipResponse {
  response: string
  prankType: string | null
  memeFileName: string | null
  gameName: string | null
  code: unknown[] | null
}
