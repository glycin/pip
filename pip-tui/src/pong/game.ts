export const FIELD_W = 40
export const FIELD_H = 15
export const PADDLE_H = 3
export const WIN_SCORE = 5

export type PongState = {
  ball: {x: number; y: number; vx: number; vy: number}
  playerY: number
  cpuY: number
  score: {player: number; cpu: number}
  status: 'playing' | 'won' | 'lost'
}

const BALL_SPEED_X = 0.8
const BALL_SPEED_Y = 0.4
const CPU_SPEED = 0.45
const PLAYER_STEP = 1

function serve(towardPlayer: boolean): PongState['ball'] {
  const vy = (Math.random() < 0.5 ? -1 : 1) * BALL_SPEED_Y
  return {
    x: FIELD_W / 2,
    y: FIELD_H / 2,
    vx: towardPlayer ? -BALL_SPEED_X : BALL_SPEED_X,
    vy,
  }
}

export function initial(): PongState {
  return {
    ball: serve(false),
    playerY: (FIELD_H - PADDLE_H) / 2,
    cpuY: (FIELD_H - PADDLE_H) / 2,
    score: {player: 0, cpu: 0},
    status: 'playing',
  }
}

function clamp(v: number, lo: number, hi: number): number {
  return Math.max(lo, Math.min(hi, v))
}

export function tick(s: PongState): PongState {
  if (s.status !== 'playing') return s

  let {x, y, vx, vy} = s.ball
  x += vx
  y += vy

  if (y < 0) { y = 0; vy = -vy }
  if (y > FIELD_H - 1) { y = FIELD_H - 1; vy = -vy }

  if (vx < 0 && x <= 1 && y >= s.playerY && y < s.playerY + PADDLE_H) {
    x = 1
    vx = -vx
    vy += ((y - (s.playerY + PADDLE_H / 2)) / (PADDLE_H / 2)) * 0.3
  }
  if (vx > 0 && x >= FIELD_W - 2 && y >= s.cpuY && y < s.cpuY + PADDLE_H) {
    x = FIELD_W - 2
    vx = -vx
    vy += ((y - (s.cpuY + PADDLE_H / 2)) / (PADDLE_H / 2)) * 0.3
  }

  let score = s.score
  let ball = {x, y, vx, vy}
  if (x < 0) {
    score = {...score, cpu: score.cpu + 1}
    ball = serve(false)
  } else if (x >= FIELD_W) {
    score = {...score, player: score.player + 1}
    ball = serve(true)
  }

  const cpuCenter = s.cpuY + PADDLE_H / 2
  let cpuY = s.cpuY
  if (ball.y < cpuCenter - 0.3) cpuY -= CPU_SPEED
  else if (ball.y > cpuCenter + 0.3) cpuY += CPU_SPEED
  cpuY = clamp(cpuY, 0, FIELD_H - PADDLE_H)

  let status: PongState['status'] = 'playing'
  if (score.player >= WIN_SCORE) status = 'won'
  else if (score.cpu >= WIN_SCORE) status = 'lost'

  return {ball, playerY: s.playerY, cpuY, score, status}
}

export function movePlayer(s: PongState, dir: -1 | 1): PongState {
  const playerY = clamp(s.playerY + dir * PLAYER_STEP, 0, FIELD_H - PADDLE_H)
  return {...s, playerY}
}
