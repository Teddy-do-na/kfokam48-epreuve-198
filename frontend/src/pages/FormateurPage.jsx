import { useState } from 'react'
import { api } from '../api/client.js'

const copy = {
  description: 'Choisissez la promotion et partagez le code avec vos étudiants.',
  error: 'Impossible de créer la session. Vérifiez les informations et réessayez.',
}

export default function FormateurPage() {
  const [titre, setTitre] = useState('')
  const [promotionId, setPromotionId] = useState('')
  const [session, setSession] = useState(null)
  const [etudiantId, setEtudiantId] = useState('')
  const [feedbackPresence, setFeedbackPresence] = useState('')
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(false)

  async function ouvrirSession(event) {
    event.preventDefault()
    setErreur('')
    setSession(null)
    setChargement(true)
    try {
      const resultat = await api.ouvrirSession({ titre: titre.trim(), promotionId: Number(promotionId) })
      setSession(resultat)
    } catch (error) {
      setErreur(error.message || copy.error)
    } finally {
      setChargement(false)
    }
  }

  async function ajouterPresence(event) {
    event.preventDefault()
    setErreur('')
    setFeedbackPresence('')
    setChargement(true)
    try {
      const result = await api.ajouterPresenceManuelle({ sessionId: session.id, etudiantId: Number(etudiantId) })
      setFeedbackPresence(`Présence ajoutée par le formateur · étudiant ${result.etudiantId}`)
      setEtudiantId('')
    } catch (error) {
      setErreur(error.message || 'Impossible d’ajouter cette présence.')
    } finally {
      setChargement(false)
    }
  }

  return (
    <main className="mx-auto grid w-full max-w-6xl gap-10 px-5 py-10 lg:grid-cols-[1fr_0.8fr] lg:px-8 lg:py-16">
      <section className="animate-fade-up">
        <span className="eyebrow">Espace formateur</span>
        <h1 className="mt-4 max-w-xl text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">Une session prête en quelques instants.</h1>
        <p className="mt-5 max-w-lg text-base leading-7 text-slate-600">{copy.description} La présence est centralisée, vos étudiants peuvent ensuite déposer leur exercice.</p>
        <form onSubmit={ouvrirSession} className="panel mt-8 space-y-5 p-6 sm:p-8">
          <label className="field-label" htmlFor="titre">Intitulé du cours
            <input id="titre" className="field-input" value={titre} onChange={event => setTitre(event.target.value)} placeholder="Ex. Architecture logicielle" required maxLength={255} />
          </label>
          <label className="field-label" htmlFor="promotionId">Identifiant de promotion
            <input id="promotionId" className="field-input" type="number" min="1" value={promotionId} onChange={event => setPromotionId(event.target.value)} placeholder="1" required />
          </label>
          {erreur && <p role="alert" className="rounded-xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{erreur}</p>}
          <button type="submit" disabled={chargement} className="button-primary w-full disabled:cursor-wait disabled:opacity-60">{chargement ? 'Ouverture en cours…' : 'Ouvrir la session'}</button>
        </form>
      </section>
      <aside className="panel relative flex min-h-80 flex-col justify-between overflow-hidden bg-slate-950 p-7 text-white sm:p-9">
        <div className="absolute -right-16 -top-16 h-56 w-56 rounded-full bg-teal-400/20 blur-3xl" />
        <div className="relative">
          <div className="flex items-center gap-2 text-sm font-medium text-teal-200"><span className="h-2 w-2 rounded-full bg-teal-300" /> Code de présence</div>
          {session ? <div className="mt-12"><p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Code de présence</p><p className="mt-3 break-all font-mono text-5xl font-semibold tracking-[0.18em] text-white sm:text-6xl">{session.code}</p><p className="mt-5 text-sm text-slate-300">Valable jusqu’au {new Date(session.expirationAt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}</p></div> : <div className="mt-12"><p className="text-2xl font-medium">Votre code apparaîtra ici.</p><p className="mt-3 max-w-sm text-sm leading-6 text-slate-400">Créez une session pour générer un code sécurisé, partageable avec la promotion.</p></div>}
        </div>
        {session && <div className="relative mt-10 space-y-4">
          <p role="status" className="rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-teal-100">Session ouverte. Le code est valable pendant 15 minutes.</p>
          <form onSubmit={ajouterPresence} className="rounded-2xl border border-white/10 bg-white/5 p-4">
            <label htmlFor="presenceManuelle" className="text-sm font-semibold text-white">Ajouter une présence manuellement</label>
            <div className="mt-3 flex gap-2">
              <input id="presenceManuelle" className="min-w-0 flex-1 rounded-xl border border-white/10 bg-white/10 px-3 py-2 text-sm text-white placeholder:text-slate-400" type="number" min="1" required value={etudiantId} onChange={event => setEtudiantId(event.target.value)} placeholder="Identifiant étudiant" />
              <button disabled={chargement} className="rounded-xl bg-teal-400 px-3 py-2 text-sm font-semibold text-slate-950 disabled:opacity-50">Ajouter</button>
            </div>
          </form>
          {feedbackPresence && <p role="status" className="rounded-xl bg-teal-400/10 px-4 py-3 text-sm text-teal-100">{feedbackPresence}</p>}
        </div>}
      </aside>
    </main>
  )
}
