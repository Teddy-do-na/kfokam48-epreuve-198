import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RelecteurPage from './RelecteurPage.jsx'
import { api } from '../api/client.js'

vi.mock('../api/client.js', () => ({ api: { soumettreRelecture: vi.fn() } }))

describe('RelecteurPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('refuse une note hors bornes', async () => {
    render(<RelecteurPage />)
    fireEvent.change(screen.getByLabelText('Identifiant de relecture'), { target: { value: '5' } })
    fireEvent.change(screen.getByLabelText('Votre identifiant étudiant'), { target: { value: '8' } })
    fireEvent.change(screen.getByLabelText('Note sur 20'), { target: { value: '21' } })
    fireEvent.change(screen.getByLabelText('Commentaire'), { target: { value: 'Commentaire utile' } })
    fireEvent.submit(screen.getByRole('button', { name: 'Enregistrer ma relecture' }).closest('form'))
    expect((await screen.findByRole('alert')).textContent).toContain('entier compris entre 0 et 20')
  })

  it('confirme la relecture et annonce la possibilité de corriger avant clôture', async () => {
    api.soumettreRelecture.mockResolvedValue(undefined)
    render(<RelecteurPage />)
    fireEvent.change(screen.getByLabelText('Identifiant de relecture'), { target: { value: '5' } })
    fireEvent.change(screen.getByLabelText('Votre identifiant étudiant'), { target: { value: '8' } })
    fireEvent.change(screen.getByLabelText('Note sur 20'), { target: { value: '18' } })
    fireEvent.change(screen.getByLabelText('Commentaire'), { target: { value: 'Très bon travail' } })
    fireEvent.click(screen.getByRole('button', { name: 'Enregistrer ma relecture' }))
    expect((await screen.findByRole('status')).textContent).toContain('corriger avant la clôture')
  })
})
