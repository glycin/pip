import {readdirSync, readFileSync} from 'node:fs'
import {dirname, join} from 'node:path'
import {fileURLToPath} from 'node:url'

export type PipState = 'SLEEPING' | 'THINKING' | 'IDLE' | 'PONG'

const assetsDir = join(
  dirname(fileURLToPath(import.meta.url)),
  '..',
  'assets',
  'cat',
)

const idxRe = /^(\d+)\.txt$/
const frameIdx = (name: string) =>
  parseInt(name.match(idxRe)?.[1] ?? '0', 10)

function loadFrames(state: PipState): string[] {
  const dir = join(assetsDir, state.toLowerCase())
  const files = readdirSync(dir)
    .filter(f => idxRe.test(f))
    .sort((a, b) => frameIdx(a) - frameIdx(b))
  if (files.length === 0) {
    throw new Error(
      `no frames found for state ${state} in ${dir} (expected <n>.txt)`,
    )
  }
  return files.map(f => readFileSync(join(dir, f), 'utf8').replace(/\n$/, ''))
}

export const CAT_FRAMES: Record<PipState, string[]> = {
  SLEEPING: loadFrames('SLEEPING'),
  THINKING: loadFrames('THINKING'),
  IDLE: loadFrames('IDLE'),
  PONG: loadFrames('PONG'),
}

export const FRAME_INTERVAL_MS: Record<PipState, number> = {
  SLEEPING: 700,
  THINKING: 300,
  IDLE: 200,
  PONG: 200,
}
