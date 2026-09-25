import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
})

client.interceptors.response.use(
  response => response,
  error => Promise.reject(error.response?.data ?? { code: 'ERREUR_RESEAU', message: 'Le service est momentanément indisponible.' }),
)

export const api = {
  ouvrirSession: async data => (await client.post('/sessions', data)).data,
  marquerPresence: async data => (await client.post('/presences', data)).data,
  deposerExercice: async data => (await client.post('/exercices', data)).data,
  remplacerExercice: async (id, data) => (await client.put(`/exercices/${id}`, data)).data,
  soumettreRelecture: async (id, data) => (await client.post(`/relectures/${id}`, data)).data,
  getRelecturesEtudiant: async etudiantId => (await client.get(`/etudiants/${etudiantId}/relectures`)).data,
  getTableau: async promotionId => (await client.get('/tableau', { params: { promotionId } })).data,
  ajouterPresenceManuelle: async data => (await client.post('/presences/manuelle', data)).data,
  cloturerSession: async id => (await client.post(`/sessions/${id}/cloture`)).data,
  getSessions: async promotionId => (await client.get('/sessions', { params: { promotionId } })).data,
  login: async data => (await client.post('/auth/login', data)).data,
  inscrireEtudiant: async data => (await client.post('/auth/inscription', data)).data,
}
