import React, {useState} from 'react'
import {Box, Text} from 'ink'
import TextInput from 'ink-text-input'

export function InputLine({
  onSubmit,
  disabled,
}: {
  onSubmit: (s: string) => void
  disabled?: boolean
}) {
  const [value, setValue] = useState('')
  return (
    <Box>
      <Text>{'> '}</Text>
      {disabled ? (
        <Text dimColor>(pip is thinking...)</Text>
      ) : (
        <TextInput
          value={value}
          onChange={setValue}
          onSubmit={v => {
            if (!v.trim()) return
            onSubmit(v)
            setValue('')
          }}
        />
      )}
    </Box>
  )
}
