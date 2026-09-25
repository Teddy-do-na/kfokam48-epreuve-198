import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EtudiantPage from './EtudiantPage.jsx'
import { api } from '../api/client.js'

vi.mock('../api/client.js', () => ({
  api: { marquerPresence: vi.fn(), deposerExercice: vi.fn() },
}))

describe('EtudiantPage attendance expiration', () => {
  beforeEach(() => vi.clearAllMocks())

  it('affiche le message renvoyé lorsque le code a expiré', async () => {
    api.marquerPresence.mockRejectedValue({ code: 'CODE_EXPIRE', message: 'Le code de présence a expiré.' })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Code de présence'), { target: { value: 'ABC234' } })
    fireEvent.click(screen.getByRole('button', { name: 'Valider ma présence' }))

    expect((await screen.findByRole('alert')).textContent).toBe('Le code de présence a expiré.')
  })
})
