import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EtudiantPage from './EtudiantPage.jsx'
import { api } from '../api/client.js'

vi.mock('../api/client.js', () => ({
  api: { marquerPresence: vi.fn(), deposerExercice: vi.fn() },
}))

describe('EtudiantPage attendance', () => {
  beforeEach(() => vi.clearAllMocks())

  it('confirme la présence après validation', async () => {
    api.marquerPresence.mockResolvedValue({ id: 1, sessionId: 3, etudiantId: 9, source: 'ETUDIANT' })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Code de présence'), { target: { value: 'abc234' } })
    fireEvent.click(screen.getByRole('button', { name: 'Valider ma présence' }))
    expect((await screen.findByRole('status')).textContent).toBe('Votre présence a bien été enregistrée.')
    expect(api.marquerPresence).toHaveBeenCalledWith({ code: 'ABC234', etudiantId: 9 })
  })

  it('affiche le refus lorsque la présence est déjà enregistrée', async () => {
    api.marquerPresence.mockRejectedValue({ code: 'DEJA_PRESENT', message: 'La présence de cet étudiant est déjà enregistrée.' })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Code de présence'), { target: { value: 'ABC234' } })
    fireEvent.click(screen.getByRole('button', { name: 'Valider ma présence' }))
    expect((await screen.findByRole('alert')).textContent).toContain('déjà enregistrée')
  })

  it('indique à l’étudiant le délai restant après cinq erreurs', async () => {
    api.marquerPresence.mockRejectedValue({ code: 'ETUDIANT_BLOQUE', message: 'Trop de codes incorrects.', reessayerDansSecondes: 68 })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Code de présence'), { target: { value: 'ABC234' } })
    fireEvent.click(screen.getByRole('button', { name: 'Valider ma présence' }))
    expect((await screen.findByRole('alert')).textContent).toContain('Réessayez dans 68 secondes.')
  })
})
