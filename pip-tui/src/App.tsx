import React, {useEffect, useRef, useState} from 'react'
import {Box} from 'ink'
import {randomBytes} from 'node:crypto'
import {Cat} from './Cat.js'
import {SpeechBubble} from './SpeechBubble.js'
import {InputLine} from './InputLine.js'
import {Pong} from './pong/Pong.js'
import {useTypewriter} from './useTypewriter.js'
import {CAT_FRAMES, FRAME_INTERVAL_MS, PipState} from './state.js'
import {help} from './http/client.js'

const nanoId = () => randomBytes(10).toString('hex')

type View = 'chat' | 'pong'

const PONG_INTRO_MS = 3000
const INTRO_DURATION_MS = CAT_FRAMES.INTRO.length * FRAME_INTERVAL_MS.INTRO

export function App() {
  const [state, setState] = useState<PipState>('INTRO')
  const [answer, setAnswer] = useState<string>("can't you see I'm sleeping? Meow")
  const [chatId] = useState<string>(() => nanoId())
  const [view, setView] = useState<View>('chat')
  const revealed = useTypewriter(answer, 60)
  const pongTimer = useRef<NodeJS.Timeout | null>(null)

  useEffect(() => () => {
    if (pongTimer.current) clearTimeout(pongTimer.current)
  }, [])

  useEffect(() => {
    const t = setTimeout(() => setState('SLEEPING'), INTRO_DURATION_MS)
    return () => clearTimeout(t)
  }, [])

  const schedulePong = () => {
    pongTimer.current = setTimeout(() => {
      pongTimer.current = null
      setState('PONG')
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

  const exitPong = () => {
    setView('chat')
    setState('IDLE')
  }

  const showBubble = view === 'chat' && state !== 'THINKING' && state !== 'INTRO'
  const inputDisabled = state === 'THINKING' || state === 'INTRO' || view === 'pong'

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
