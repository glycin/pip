import {useEffect, useState} from 'react'

export function useTypewriter(text: string, cps = 50): string {
  const [n, setN] = useState(0)

  useEffect(() => {
    setN(0)
    if (!text) return
    const ms = Math.max(10, Math.floor(1000 / cps))
    const h = setInterval(() => {
      setN(prev => {
        if (prev >= text.length) {
          clearInterval(h)
          return prev
        }
        return prev + 1
      })
    }, ms)
    return () => clearInterval(h)
  }, [text, cps])

  return text.slice(0, n)
}
