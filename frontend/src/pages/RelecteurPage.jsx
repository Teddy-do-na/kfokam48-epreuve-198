import { useState } from 'react'
import { api } from '../api/client.js'

export default function RelecteurPage() {
  const [etudiantId, setEtudiantId] = useState('')
  const [relectures, setRelectures] = useState([])
  const [relecturesChargees, setRelecturesChargees] = useState(false)
  const [relecture, setRelecture] = useState(null)
  const [note, setNote] = useState('')
  const [commentaire, setCommentaire] = useState('')
  const [message, setMessage] = useState('')
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(false)

  async function chargerRelectures() {
    setErreur('')
    setMessage('')
    setRelecture(null)
    setRelectures([])
    setRelecturesChargees(false)
    if (!etudiantId || Number(etudiantId) < 1) {
      setErreur('Renseignez votre identifiant étudiant pour charger vos relectures.')
      return
    }
    setChargement(true)
    try {
      setRelectures(await api.getRelecturesAssignees(Number(etudiantId)))
      setRelecturesChargees(true)
    } catch (error) {
      setErreur(error.message || 'Impossible de charger vos relectures. Vérifiez votre identifiant.')
    } finally {
      setChargement(false)
    }
  }

  function selectionner(relectureSelectionnee) {
    setRelecture(relectureSelectionnee)
    setNote(relectureSelectionnee.note == null ? '' : String(relectureSelectionnee.note))
    setCommentaire(relectureSelectionnee.commentaire || '')
    setMessage('')
    setErreur('')
  }

  async function soumettre(event) {
    event.preventDefault()
    setErreur('')
    setMessage('')
    if (!relecture) {
      setErreur('Sélectionnez une relecture avant de l’enregistrer.')
      return
    }
    if (note.trim() === '' || Number(note) < 0 || Number(note) > 20 || !Number.isInteger(Number(note))) {
      setErreur('La note doit être un nombre entier compris entre 0 et 20.')
      return
    }
    setChargement(true)
    try {
      await api.soumettreRelecture(relecture.id, { etudiantId: Number(etudiantId), note: Number(note), commentaire: commentaire.trim() })
      const relectureMiseAJour = { ...relecture, note: Number(note), commentaire: commentaire.trim(), statut: 'RENDUE' }
      setRelecture(relectureMiseAJour)
      setRelectures(precedentes => precedentes.map(item => item.id === relecture.id ? relectureMiseAJour : item))
      setMessage('Votre relecture a été enregistrée. Vous pouvez la corriger avant la clôture de la session.')
    } catch (error) {
      setErreur(error.message || 'Impossible d’enregistrer la relecture. Vérifiez vos informations.')
    } finally {
      setChargement(false)
    }
  }

  return (
    <main className="mx-auto grid w-full max-w-6xl gap-10 px-5 py-10 lg:grid-cols-[0.9fr_1.1fr] lg:px-8 lg:py-16">
      <section className="animate-fade-up">
        <span className="eyebrow">Espace relecteur</span>
        <h1 className="mt-4 text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">Un retour juste fait progresser chacun.</h1>
        <p className="mt-5 max-w-lg text-base leading-7 text-slate-600">Évaluez le travail d’un pair avec attention. Votre identité reste confidentielle auprès de l’étudiant relu.</p>
        <div className="mt-8 rounded-2xl border border-teal-100 bg-teal-50 p-5"><p className="text-sm font-semibold text-teal-950">Repères de notation</p><p className="mt-2 text-sm leading-6 text-teal-800">Notez sur 20 et accompagnez votre évaluation d’un commentaire clair, précis et constructif.</p></div>
      </section>
      <section className="space-y-5">
        <form className="panel space-y-5 p-6 sm:p-8" onSubmit={event => { event.preventDefault(); chargerRelectures() }}>
          <h2 className="text-xl font-semibold text-slate-950">Vos relectures assignées</h2>
          <label className="field-label" htmlFor="etudiantId">Votre identifiant étudiant
            <input className="field-input" id="etudiantId" type="number" min="1" required value={etudiantId} onChange={event => { setEtudiantId(event.target.value); setRelectures([]); setRelecturesChargees(false); setRelecture(null) }} placeholder="Votre identifiant" />
          </label>
          <button className="button-secondary w-full" type="submit" disabled={chargement}>{chargement ? 'Chargement…' : 'Charger mes relectures'}</button>
          {relecturesChargees && relectures.length === 0 && <p role="status" className="text-sm text-slate-600">Aucune relecture ne vous est assignée pour le moment.</p>}
          {relectures.length > 0 && <ul className="space-y-3" aria-label="Liste des relectures">
            {relectures.map(item => <li key={item.id}>
              <button type="button" onClick={() => selectionner(item)} aria-pressed={relecture?.id === item.id} className={`w-full rounded-xl border p-4 text-left transition ${relecture?.id === item.id ? 'border-teal-500 bg-teal-50' : 'border-slate-200 hover:border-teal-300'}`}>
                <span className="block text-sm font-semibold text-slate-900">Exercice · relecture #{item.id}</span>
                <span className="mt-1 block truncate text-sm text-slate-600">{item.lien}</span>
                <span className="mt-2 block text-xs font-medium text-teal-800">{item.statut === 'RENDUE' ? 'Relecture rendue · modifiable avant clôture' : 'À relire'}</span>
              </button>
            </li>)}
          </ul>}
        </form>
        {relecture && <form className="panel space-y-5 p-6 sm:p-8" onSubmit={soumettre}>
          <h2 className="text-xl font-semibold text-slate-950">{relecture.statut === 'RENDUE' ? 'Corriger ma relecture' : 'Votre relecture'}</h2>
          <p className="text-sm text-slate-600">Travail à évaluer : <a className="break-all font-medium text-teal-800 underline" href={relecture.lien} target="_blank" rel="noreferrer">{relecture.lien}</a></p>
          <label className="field-label" htmlFor="note">Note sur 20
            <input className="field-input" id="note" type="number" min="0" max="20" step="1" required value={note} onChange={event => setNote(event.target.value)} placeholder="15" />
          </label>
          <label className="field-label" htmlFor="commentaire">Commentaire
            <textarea className="field-input min-h-32 resize-y" id="commentaire" required maxLength={4000} value={commentaire} onChange={event => setCommentaire(event.target.value)} placeholder="Présentez les points forts et les pistes de progression…" />
          </label>
          {erreur && <p role="alert" className="rounded-xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{erreur}</p>}
          {message && <p role="status" className="rounded-xl bg-teal-50 px-4 py-3 text-sm font-medium text-teal-800">{message}</p>}
          <button className="button-primary w-full" disabled={chargement}>{chargement ? 'Enregistrement…' : relecture.statut === 'RENDUE' ? 'Enregistrer la correction' : 'Enregistrer ma relecture'}</button>
        </form>}
        {erreur && !relecture && <p role="alert" className="rounded-xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{erreur}</p>}
      </section>
    </main>
  )
}
