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
  const [tableauPromotionId, setTableauPromotionId] = useState('')
  const [tableau, setTableau] = useState([])
  const [tableauCharge, setTableauCharge] = useState(false)
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

  async function chargerTableau(event) {
    event.preventDefault()
    setErreur('')
    setTableauCharge(false)
    if (!tableauPromotionId || Number(tableauPromotionId) < 1) {
      setErreur('Renseignez un identifiant de promotion valide.')
      return
    }
    setChargement(true)
    try {
      setTableau(await api.getTableau(Number(tableauPromotionId)))
      setTableauCharge(true)
    } catch (error) {
      setErreur(error.message || 'Impossible de charger le tableau de suivi.')
      setTableau([])
    } finally {
      setChargement(false)
    }
  }

  return (
    <main className="mx-auto w-full max-w-6xl space-y-10 px-5 py-10 lg:px-8 lg:py-16">
      <div className="grid gap-10 lg:grid-cols-[1fr_0.8fr]">
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
      </div>
      <section className="panel space-y-5 p-6 sm:p-8">
        <div>
          <span className="eyebrow">Suivi pédagogique</span>
          <h2 className="mt-3 text-2xl font-semibold text-slate-950">Tableau de suivi de la promotion</h2>
          <p className="mt-2 text-sm text-slate-600">Présences, dépôts, moyennes et relectures restant à rendre.</p>
        </div>
        <form className="flex flex-col gap-3 sm:flex-row" onSubmit={chargerTableau}>
          <label className="field-label min-w-0 flex-1" htmlFor="tableauPromotionId">Promotion à consulter
            <input className="field-input" id="tableauPromotionId" type="number" min="1" required value={tableauPromotionId} onChange={event => setTableauPromotionId(event.target.value)} placeholder="1" />
          </label>
          <button className="button-primary self-end" type="submit" disabled={chargement}>{chargement ? 'Chargement…' : 'Afficher le tableau'}</button>
        </form>
        {tableauCharge && <div className="overflow-x-auto rounded-xl border border-slate-200">
          <table className="w-full min-w-[680px] text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500"><tr><th className="px-4 py-3">Étudiant</th><th className="px-4 py-3">Présences</th><th className="px-4 py-3">Exercices</th><th className="px-4 py-3">Moyenne</th><th className="px-4 py-3">Relectures en attente</th></tr></thead>
            <tbody className="divide-y divide-slate-100 bg-white">{tableau.map(etudiant => <tr key={etudiant.etudiantId}><th scope="row" className="px-4 py-3 font-semibold text-slate-900">{etudiant.nom}</th><td className="px-4 py-3">{etudiant.presences}</td><td className="px-4 py-3">{etudiant.exercicesDeposes}</td><td className="px-4 py-3">{etudiant.moyenne == null ? '—' : `${Number(etudiant.moyenne).toFixed(1)} / 20`}</td><td className="px-4 py-3">{etudiant.relecturesEnAttente}</td></tr>)}</tbody>
          </table>
          {tableau.length === 0 && <p className="px-4 py-5 text-sm text-slate-600">Aucun étudiant dans cette promotion.</p>}
        </div>}
      </section>
    </main>
  )
}
