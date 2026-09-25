import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import FormateurPage from './FormateurPage.jsx'
import { api } from '../api/client.js'

vi.mock('../api/client.js', () => ({
  api: { ouvrirSession: vi.fn() },
}))

describe('FormateurPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('affiche le code et son expiration après la création', async () => {
    api.ouvrirSession.mockResolvedValue({ id: 4, code: 'AB23CD', ouvertureAt: '2026-01-01T10:00:00Z', expirationAt: '2026-01-01T10:15:00Z' })
    render(<FormateurPage />)
    fireEvent.change(screen.getByLabelText('Intitulé du cours'), { target: { value: 'Développement web' } })
    fireEvent.change(screen.getByLabelText('Identifiant de promotion'), { target: { value: '2' } })
    fireEvent.click(screen.getByRole('button', { name: 'Ouvrir la session' }))

    expect((await screen.findByText('AB23CD')).textContent).toBe('AB23CD')
    expect(api.ouvrirSession).toHaveBeenCalledWith({ titre: 'Développement web', promotionId: 2 })
  })

  it('affiche le message d’erreur de l’API', async () => {
    api.ouvrirSession.mockRejectedValue({ message: 'Promotion introuvable.' })
    render(<FormateurPage />)
    fireEvent.change(screen.getByLabelText('Intitulé du cours'), { target: { value: 'Développement web' } })
    fireEvent.change(screen.getByLabelText('Identifiant de promotion'), { target: { value: '2' } })
    fireEvent.click(screen.getByRole('button', { name: 'Ouvrir la session' }))

    await waitFor(() => expect(screen.getByRole('alert').textContent).toContain('Promotion introuvable.'))
  })
})
