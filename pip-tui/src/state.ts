import {readdirSync, readFileSync} from 'node:fs'
import {dirname, join} from 'node:path'
import {fileURLToPath} from 'node:url'

export type PipState = 'SLEEPING' | 'THINKING' | 'IDLE'

const assetsDir = join(
  dirname(fileURLToPath(import.meta.url)),
  '..',
  'assets',
  'cat',
)

const idxRe = /_(\d+)\.txt$/
const frameIdx = (name: string) =>
  parseInt(name.match(idxRe)?.[1] ?? '0', 10)

function loadFrames(state: PipState): string[] {
  const prefix = state.toLowerCase() + '_'
  const files = readdirSync(assetsDir)
    .filter(f => f.toLowerCase().startsWith(prefix) && f.endsWith('.txt'))
    .sort((a, b) => frameIdx(a) - frameIdx(b))
  if (files.length === 0) {
    throw new Error(
      `no frames found for state ${state} in ${assetsDir} (expected ${prefix}<n>.txt)`,
    )
  }
  return files.map(f => readFileSync(join(assetsDir, f), 'utf8').replace(/\n$/, ''))
}

export const CAT_FRAMES: Record<PipState, string[]> = {
  SLEEPING: loadFrames('SLEEPING'),
  THINKING: loadFrames('THINKING'),
  IDLE: loadFrames('IDLE'),
}

export const FRAME_INTERVAL_MS: Record<PipState, number> = {
  SLEEPING: 700,
  THINKING: 300,
  IDLE: 1500,
}
