import React, { useState } from 'react'
import useDialogState from '@/hooks/use-dialog-state'
import { type Post } from '../data/schema'

export type TasksDialogType = 'create' | 'update' | 'delete' | 'import' | 'view'

type TasksContextType = {
  open: TasksDialogType | null
  setOpen: (str: TasksDialogType | null) => void
  currentRow: Post | null
  setCurrentRow: React.Dispatch<React.SetStateAction<Post | null>>
}

const TasksContext = React.createContext<TasksContextType | null>(null)

export function TasksProvider({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useDialogState<TasksDialogType>(null)
  const [currentRow, setCurrentRow] = useState<Post | null>(null)

  return (
    <TasksContext.Provider value={{ open, setOpen, currentRow, setCurrentRow }}>
      {children}
    </TasksContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useTasks = () => {
  const tasksContext = React.useContext(TasksContext)

  if (!tasksContext) {
    throw new Error('useTasks has to be used within <TasksContext>')
  }

  return tasksContext
}
