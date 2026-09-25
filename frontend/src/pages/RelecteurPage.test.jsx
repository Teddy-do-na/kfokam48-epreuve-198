import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RelecteurPage from './RelecteurPage.jsx'
import { api } from '../api/client.js'

vi.mock('../api/client.js', () => ({ api: { getRelecturesAssignees: vi.fn(), soumettreRelecture: vi.fn() } }))

describe('RelecteurPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('refuse une note hors bornes', async () => {
    api.getRelecturesAssignees.mockResolvedValue([{ id: 5, lien: 'https://example.org/work', note: null, commentaire: null, statut: 'EN_ATTENTE' }])
    render(<RelecteurPage />)
    fireEvent.change(screen.getByLabelText('Votre identifiant étudiant'), { target: { value: '8' } })
    fireEvent.click(screen.getByRole('button', { name: 'Charger mes relectures' }))
    fireEvent.click(await screen.findByRole('button', { name: /Exercice · relecture #5/ }))
    fireEvent.change(screen.getByLabelText('Note sur 20'), { target: { value: '21' } })
    fireEvent.change(screen.getByLabelText('Commentaire'), { target: { value: 'Commentaire utile' } })
    fireEvent.submit(screen.getByRole('button', { name: 'Enregistrer ma relecture' }).closest('form'))
    expect((await screen.findByRole('alert')).textContent).toContain('entier compris entre 0 et 20')
    expect(api.soumettreRelecture).not.toHaveBeenCalled()
  })

  it('charge une affectation et confirme la première soumission', async () => {
    api.getRelecturesAssignees.mockResolvedValue([{ id: 5, lien: 'https://example.org/work', note: null, commentaire: null, statut: 'EN_ATTENTE' }])
    api.soumettreRelecture.mockResolvedValue(undefined)
    render(<RelecteurPage />)
    fireEvent.change(screen.getByLabelText('Votre identifiant étudiant'), { target: { value: '8' } })
    fireEvent.click(screen.getByRole('button', { name: 'Charger mes relectures' }))
    fireEvent.click(await screen.findByRole('button', { name: /Exercice · relecture #5/ }))
    fireEvent.change(screen.getByLabelText('Note sur 20'), { target: { value: '18' } })
    fireEvent.change(screen.getByLabelText('Commentaire'), { target: { value: 'Très bon travail' } })
    fireEvent.click(screen.getByRole('button', { name: 'Enregistrer ma relecture' }))
    expect((await screen.findByRole('status')).textContent).toContain('corriger avant la clôture')
    expect(api.soumettreRelecture).toHaveBeenCalledWith(5, { etudiantId: 8, note: 18, commentaire: 'Très bon travail' })
    expect(await screen.findByRole('button', { name: 'Enregistrer la correction' })).toBeTruthy()
  })

  it('préremplit la relecture rendue et envoie la correction', async () => {
    api.getRelecturesAssignees.mockResolvedValue([{ id: 5, lien: 'https://example.org/work', note: 18, commentaire: 'Premier retour', statut: 'RENDUE' }])
    api.soumettreRelecture.mockResolvedValue(undefined)
    render(<RelecteurPage />)
    fireEvent.change(screen.getByLabelText('Votre identifiant étudiant'), { target: { value: '8' } })
    fireEvent.click(screen.getByRole('button', { name: 'Charger mes relectures' }))
    fireEvent.click(await screen.findByRole('button', { name: /Exercice · relecture #5/ }))
    expect(screen.getByLabelText('Note sur 20').value).toBe('18')
    expect(screen.getByLabelText('Commentaire').value).toBe('Premier retour')
    fireEvent.change(screen.getByLabelText('Note sur 20'), { target: { value: '19' } })
    fireEvent.change(screen.getByLabelText('Commentaire'), { target: { value: 'Retour corrigé' } })
    fireEvent.click(screen.getByRole('button', { name: 'Enregistrer la correction' }))
    await waitFor(() => expect(api.soumettreRelecture).toHaveBeenCalledWith(5, { etudiantId: 8, note: 19, commentaire: 'Retour corrigé' }))
    expect(await screen.findByRole('status')).toBeTruthy()
  })

  it('indique quand aucune relecture n’est assignée', async () => {
    api.getRelecturesAssignees.mockResolvedValue([])
    render(<RelecteurPage />)
    fireEvent.change(screen.getByLabelText('Votre identifiant étudiant'), { target: { value: '8' } })
    fireEvent.click(screen.getByRole('button', { name: 'Charger mes relectures' }))
    expect(await screen.findByText('Aucune relecture ne vous est assignée pour le moment.')).toBeTruthy()
  })
})
