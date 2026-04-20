import React from 'react'
import {Box, Text} from 'ink'

function wrap(text: string, width: number): string[] {
  const words = text.split(/\s+/).filter(Boolean)
  if (words.length === 0) return ['']
  const lines: string[] = []
  let line = ''
  for (const w of words) {
    const candidate = line ? `${line} ${w}` : w
    if (candidate.length > width && line) {
      lines.push(line)
      line = w
    } else {
      line = candidate
    }
  }
  if (line) lines.push(line)
  return lines
}

export function SpeechBubble({
  text,
  width = 48,
}: {
  text: string
  width?: number
}) {
  const innerWidth = Math.max(10, width - 4)
  const lines = wrap(text, innerWidth)
  const top = '╭' + '─'.repeat(width - 2) + '╮'
  const bot = '╰' + '─'.repeat(3) + '┬' + '─'.repeat(width - 6) + '╯'
  return (
    <Box flexDirection="column">
      <Text>{top}</Text>
      {lines.map((l, i) => (
        <Text key={i}>│ {l.padEnd(innerWidth)} │</Text>
      ))}
      <Text>{bot}</Text>
      <Text>    ▼</Text>
    </Box>
  )
}
