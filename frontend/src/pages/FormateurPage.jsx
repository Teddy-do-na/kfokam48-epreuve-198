import { useState } from 'react'
import { api } from '../api/client.js'

const copy = {
  title: 'Préparer une nouvelle session',
  description: 'Choisissez la promotion et partagez le code avec vos étudiants.',
  courseLabel: 'Intitulé du cours',
  coursePlaceholder: 'Ex. Architecture logicielle',
  promotionLabel: 'Identifiant de promotion',
  submit: 'Ouvrir la session',
  loading: 'Ouverture en cours…',
  success: 'Session ouverte. Le code est valable pendant 15 minutes.',
  error: 'Impossible de créer la session. Vérifiez les informations et réessayez.',
  codeLabel: 'Code de présence',
  expirationLabel: 'Valable jusqu’au',
}

export default function FormateurPage() {
  const [titre, setTitre] = useState('')
  const [promotionId, setPromotionId] = useState('')
  const [session, setSession] = useState(null)
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

  return (
    <main className="mx-auto grid w-full max-w-6xl gap-10 px-5 py-10 lg:grid-cols-[1fr_0.8fr] lg:px-8 lg:py-16">
      <section className="animate-fade-up">
        <span className="eyebrow">Espace formateur</span>
        <h1 className="mt-4 max-w-xl text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">Une session prête en quelques instants.</h1>
        <p className="mt-5 max-w-lg text-base leading-7 text-slate-600">{copy.description} La présence est centralisée, vos étudiants peuvent ensuite déposer leur exercice.</p>
        <form onSubmit={ouvrirSession} className="panel mt-8 space-y-5 p-6 sm:p-8">
          <label className="field-label" htmlFor="titre">{copy.courseLabel}
            <input id="titre" className="field-input" value={titre} onChange={event => setTitre(event.target.value)} placeholder={copy.coursePlaceholder} required maxLength={255} />
          </label>
          <label className="field-label" htmlFor="promotionId">{copy.promotionLabel}
            <input id="promotionId" className="field-input" type="number" min="1" value={promotionId} onChange={event => setPromotionId(event.target.value)} placeholder="1" required />
          </label>
          {erreur && <p role="alert" className="rounded-xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{erreur}</p>}
          <button type="submit" disabled={chargement} className="button-primary w-full disabled:cursor-wait disabled:opacity-60">{chargement ? copy.loading : copy.submit}</button>
        </form>
      </section>
      <aside className="panel relative flex min-h-80 flex-col justify-between overflow-hidden bg-slate-950 p-7 text-white sm:p-9">
        <div className="absolute -right-16 -top-16 h-56 w-56 rounded-full bg-teal-400/20 blur-3xl" />
        <div className="relative">
          <div className="flex items-center gap-2 text-sm font-medium text-teal-200"><span className="h-2 w-2 rounded-full bg-teal-300" /> Code de présence</div>
          {session ? <div className="mt-12"><p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">{copy.codeLabel}</p><p className="mt-3 break-all font-mono text-5xl font-semibold tracking-[0.18em] text-white sm:text-6xl">{session.code}</p><p className="mt-5 text-sm text-slate-300">{copy.expirationLabel} {new Date(session.expirationAt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}</p></div> : <div className="mt-12"><p className="text-2xl font-medium">Votre code apparaîtra ici.</p><p className="mt-3 max-w-sm text-sm leading-6 text-slate-400">Créez une session pour générer un code sécurisé, partageable avec la promotion.</p></div>}
        </div>
        {session && <p role="status" className="relative mt-10 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-teal-100">{copy.success}</p>}
      </aside>
    </main>
  )
}
