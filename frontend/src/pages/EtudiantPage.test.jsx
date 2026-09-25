import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EtudiantPage from './EtudiantPage.jsx'
import { api } from '../api/client.js'

vi.mock('../api/client.js', () => ({
  api: { marquerPresence: vi.fn(), deposerExercice: vi.fn() },
}))

describe('EtudiantPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('confirme la présence après validation', async () => {
    api.marquerPresence.mockResolvedValue({ id: 1, sessionId: 3, etudiantId: 9, source: 'ETUDIANT' })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Code de présence'), { target: { value: 'abc234' } })
    fireEvent.click(screen.getByRole('button', { name: 'Valider ma présence' }))
    expect((await screen.findByRole('status')).textContent).toBe('Votre présence a bien été enregistrée.')
  })

  it('permet de déposer un lien HTTPS et confirme la réception', async () => {
    api.deposerExercice.mockResolvedValue({ id: 5, statut: 'EN_ATTENTE_RELECTURE' })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Identifiant de session'), { target: { value: '3' } })
    fireEvent.change(screen.getByLabelText('Lien de l’exercice'), { target: { value: 'https://example.org/work' } })
    fireEvent.click(screen.getByRole('button', { name: 'Envoyer mon exercice' }))

    expect((await screen.findByRole('status')).textContent).toBe('Votre lien a été transmis pour relecture.')
    expect(api.deposerExercice).toHaveBeenCalledWith({ sessionId: 3, etudiantId: 9, lien: 'https://example.org/work' })
  })

  it('affiche le refus de dépôt en double renvoyé par le serveur', async () => {
    api.deposerExercice.mockRejectedValue({ code: 'EXERCICE_DEJA_DEPOSE', message: 'Un exercice a déjà été déposé pour cette session.' })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Identifiant de session'), { target: { value: '3' } })
    fireEvent.change(screen.getByLabelText('Lien de l’exercice'), { target: { value: 'https://example.org/work' } })
    fireEvent.click(screen.getByRole('button', { name: 'Envoyer mon exercice' }))

    expect((await screen.findByRole('alert')).textContent).toContain('déjà été déposé')
  })
})
