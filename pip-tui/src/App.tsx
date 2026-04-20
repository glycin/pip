import React, {useState} from 'react'
import {Box} from 'ink'
import {randomBytes} from 'node:crypto'
import {Cat} from './Cat.js'
import {SpeechBubble} from './SpeechBubble.js'
import {InputLine} from './InputLine.js'
import {useTypewriter} from './useTypewriter.js'
import {PipState} from './state.js'
import {chat} from './http/client.js'

const nanoId = () => randomBytes(10).toString('hex')

export function App() {
  const [state, setState] = useState<PipState>('IDLE')
  const [answer, setAnswer] = useState<string>('meow, ask me something')
  const [chatId] = useState<string>(() => nanoId())
  const revealed = useTypewriter(answer, 60)

  const onSubmit = async (input: string) => {
    setState('THINKING')
    try {
      const response = await chat(input, chatId)
      setAnswer(response)
      setState('IDLE')
    } catch (e) {
      setAnswer(`(error: ${(e as Error).message})`)
      setState('IDLE')
    }
  }

  return (
    <Box flexDirection="column" paddingX={1}>
      <Box flexDirection="column" marginBottom={1}>
        {state !== 'THINKING' && <SpeechBubble text={revealed} />}
        <Cat state={state} />
      </Box>
      <InputLine onSubmit={onSubmit} disabled={state === 'THINKING'} />
    </Box>
  )
}
