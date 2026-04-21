import React, {useEffect, useState} from 'react'
import {Box, Text, useInput} from 'ink'
import {FIELD_H, FIELD_W, PADDLE_H, PongState, initial, movePlayer, tick} from './game.js'

const TICK_MS = 50
const END_LINGER_MS = 2500

const MID_X = Math.floor(FIELD_W / 2)
const TOP_BORDER = '┌' + '─'.repeat(FIELD_W) + '┐'
const BOT_BORDER = '└' + '─'.repeat(FIELD_W) + '┘'

export function Pong({onExit}: {onExit: () => void}) {
  const [s, setS] = useState<PongState>(initial)

  useEffect(() => {
    if (s.status !== 'playing') return
    const h = setInterval(() => setS(prev => tick(prev)), TICK_MS)
    return () => clearInterval(h)
  }, [s.status])

  useEffect(() => {
    if (s.status === 'playing') return
    const t = setTimeout(onExit, END_LINGER_MS)
    return () => clearTimeout(t)
  }, [s.status, onExit])

  useInput((input, key) => {
    if (input === 'q' || key.escape) {
      onExit()
      return
    }
    if (input === 'w' || key.upArrow) setS(prev => movePlayer(prev, -1))
    else if (input === 's' || key.downArrow) setS(prev => movePlayer(prev, 1))
  })

  const bx = Math.round(s.ball.x)
  const by = Math.round(s.ball.y)

  const rows: string[] = []
  for (let y = 0; y < FIELD_H; y++) {
    let line = ''
    for (let x = 0; x < FIELD_W; x++) {
      if (x === bx && y === by) line += '●'
      else if (x === 0 && y >= s.playerY && y < s.playerY + PADDLE_H) line += '█'
      else if (x === FIELD_W - 1 && y >= s.cpuY && y < s.cpuY + PADDLE_H) line += '█'
      else if (x === MID_X) line += '┊'
      else line += ' '
    }
    rows.push(line)
  }

  const status =
    s.status === 'won' ? 'YOU WIN!' :
    s.status === 'lost' ? 'AI WINS!' :
    'w/s or ↑/↓  —  q/esc to quit'

  return (
    <Box flexDirection="column" marginLeft={2}>
      <Text>{`  PIP  ${s.score.player}  :  ${s.score.cpu}  AI`}</Text>
      <Text>{TOP_BORDER}</Text>
      {rows.map((r, i) => <Text key={i}>{`│${r}│`}</Text>)}
      <Text>{BOT_BORDER}</Text>
      <Text>{status}</Text>
    </Box>
  )
}
