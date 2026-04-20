import React, {useEffect, useState} from 'react'
import {Text} from 'ink'
import {CAT_FRAMES, FRAME_INTERVAL_MS, PipState} from './state.js'

export function Cat({state}: {state: PipState}) {
  const [i, setI] = useState(0)

  useEffect(() => {
    setI(0)
    const frames = CAT_FRAMES[state]
    if (frames.length <= 1) return
    const h = setInterval(() => {
      setI(prev => (prev + 1) % frames.length)
    }, FRAME_INTERVAL_MS[state])
    return () => clearInterval(h)
  }, [state])

  const frames = CAT_FRAMES[state]
  const frame = frames[i] ?? frames[0]
  return <Text>{frame}</Text>
}
