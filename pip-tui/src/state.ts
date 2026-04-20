export type PipState = 'SLEEPING' | 'THINKING' | 'IDLE'

const CAT_EYES_OPEN_NO_MARK = `    ╱|、
  (˚ˎ 。7
   |、˜〵
  じしˍ,)ノ`

const CAT_EYES_CLOSED_NO_MARK = `    ╱|、
  (-ˎ 。7
   |、˜〵
  じしˍ,)ノ`

const catWith = (base: string, topMark: string) =>
  base.replace(/^(    ╱\|、)/, `$1 ${topMark}`)

export const CAT_FRAMES: Record<PipState, string[]> = {
  SLEEPING: [
    catWith(CAT_EYES_CLOSED_NO_MARK, 'z'),
    catWith(CAT_EYES_CLOSED_NO_MARK, ' z'),
    catWith(CAT_EYES_CLOSED_NO_MARK, '  z'),
    catWith(CAT_EYES_CLOSED_NO_MARK, '   z'),
  ],
  THINKING: [
    catWith(CAT_EYES_OPEN_NO_MARK, '?'),
    catWith(CAT_EYES_OPEN_NO_MARK, '??'),
    catWith(CAT_EYES_OPEN_NO_MARK, '???'),
  ],
  IDLE: [
    CAT_EYES_OPEN_NO_MARK,
    CAT_EYES_OPEN_NO_MARK,
    CAT_EYES_OPEN_NO_MARK,
    CAT_EYES_OPEN_NO_MARK,
    CAT_EYES_CLOSED_NO_MARK,
  ],
}

export const FRAME_INTERVAL_MS: Record<PipState, number> = {
  SLEEPING: 700,
  THINKING: 300,
  IDLE: 400,
}
