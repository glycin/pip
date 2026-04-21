import React, {useEffect, useRef, useState} from 'react'
import {Box} from 'ink'
import {randomBytes} from 'node:crypto'
import {Cat} from './Cat.js'
import {SpeechBubble} from './SpeechBubble.js'
import {InputLine} from './InputLine.js'
import {Pong} from './pong/Pong.js'
import {useTypewriter} from './useTypewriter.js'
import {PipState} from './state.js'
import {help} from './http/client.js'

const nanoId = () => randomBytes(10).toString('hex')

type View = 'chat' | 'pong'

const PONG_INTRO_MS = 3000

export function App() {
  const [state, setState] = useState<PipState>('IDLE')
  const [answer, setAnswer] = useState<string>('meow, ask me something')
  const [chatId] = useState<string>(() => nanoId())
  const [view, setView] = useState<View>('chat')
  const revealed = useTypewriter(answer, 60)
  const pongTimer = useRef<NodeJS.Timeout | null>(null)

  useEffect(() => () => {
    if (pongTimer.current) clearTimeout(pongTimer.current)
  }, [])

  const schedulePong = () => {
    pongTimer.current = setTimeout(() => {
      pongTimer.current = null
      setView('pong')
    }, PONG_INTRO_MS)
  }

  const onSubmit = async (input: string) => {
    if (pongTimer.current) {
      clearTimeout(pongTimer.current)
      pongTimer.current = null
    }
    if (input.trim().toLowerCase() === '/pong') {
      setAnswer("Let's play pong!")
      setState('IDLE')
      schedulePong()
      return
    }
    setState('THINKING')
    try {
      const res = await help(input, chatId)
      setAnswer(res.response)
      setState('IDLE')
      if (res.gameName === 'PONG') schedulePong()
    } catch (e) {
      setAnswer(`(error: ${(e as Error).message})`)
      setState('IDLE')
    }
  }

  const exitPong = () => setView('chat')

  const showBubble = view === 'chat' && state !== 'THINKING'
  const inputDisabled = state === 'THINKING' || view === 'pong'

  return (
    <Box flexDirection="row" paddingX={1}>
      <Box flexDirection="column" marginRight={2}>
        <Box flexDirection="column" marginBottom={1}>
          {showBubble && <SpeechBubble text={revealed} />}
          <Cat state={state} />
        </Box>
        <InputLine onSubmit={onSubmit} disabled={inputDisabled} />
      </Box>
      {view === 'pong' && <Pong onExit={exitPong} />}
    </Box>
  )
}
