import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import RelecteurPage from './RelecteurPage.jsx'

describe('RelecteurPage assignments', () => {
  it('explique qu’aucun exercice ne peut être attribué sans pair présent', () => {
    render(<RelecteurPage />)
    expect(screen.getByText('Aucune relecture attribuée pour le moment')).toBeTruthy()
    expect(screen.getByText(/réessayée lors d’une nouvelle présence/)).toBeTruthy()
  })
})
