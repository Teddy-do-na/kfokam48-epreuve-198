import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EtudiantPage from './EtudiantPage.jsx'
import { api } from '../api/client.js'

vi.mock('../api/client.js', () => ({
  api: { marquerPresence: vi.fn(), deposerExercice: vi.fn(), remplacerExercice: vi.fn() },
}))

describe('EtudiantPage exercise', () => {
  beforeEach(() => vi.clearAllMocks())

  it('dépose un lien et permet de le remplacer tant que la relecture est en attente', async () => {
    api.deposerExercice.mockResolvedValue({ id: 5, statut: 'EN_ATTENTE_RELECTURE' })
    api.remplacerExercice.mockResolvedValue({ id: 5, statut: 'EN_ATTENTE_RELECTURE' })
    render(<EtudiantPage />)
    fireEvent.change(screen.getByLabelText('Identifiant étudiant'), { target: { value: '9' } })
    fireEvent.change(screen.getByLabelText('Identifiant de session'), { target: { value: '3' } })
    fireEvent.change(screen.getByLabelText('Lien de l’exercice'), { target: { value: 'https://example.org/work' } })
    fireEvent.click(screen.getByRole('button', { name: 'Envoyer mon exercice' }))
    expect((await screen.findByRole('status')).textContent).toBe('Votre lien a été transmis pour relecture.')

    fireEvent.change(screen.getByLabelText('Lien de l’exercice'), { target: { value: 'https://example.org/new-work' } })
    fireEvent.click(screen.getByRole('button', { name: 'Remplacer mon lien' }))
    expect(await screen.findByText(/mis à jour tant que la relecture/)).toBeTruthy()
    expect(api.remplacerExercice).toHaveBeenCalledWith('5', { etudiantId: 9, lien: 'https://example.org/new-work' })
  })
})
