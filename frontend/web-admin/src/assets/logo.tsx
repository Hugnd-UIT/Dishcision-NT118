import * as React from 'react'
import logoOrange from './logo.png'
import logoWhite from './logo-white.png'
import { cn } from '@/lib/utils'

export function Logo({
  className,
  ...props
}: React.ImgHTMLAttributes<HTMLImageElement>) {
  return (
    <img
      src={logoOrange}
      alt='Dishcision'
      className={cn('size-7 object-contain', className)}
      {...props}
    />
  )
}

export function DishcisionLogo({
  className,
  variant = 'white',
  ...props
}: React.ImgHTMLAttributes<HTMLImageElement> & { variant?: 'orange' | 'white' }) {
  return (
    <img
      src={variant === 'orange' ? logoOrange : logoWhite}
      alt='Dishcision'
      className={cn('size-5 object-contain', className)}
      {...props}
    />
  )
}
