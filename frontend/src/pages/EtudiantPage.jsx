import { useState } from 'react'
import { api } from '../api/client.js'

const messages = {
  missing: 'Renseignez votre identifiant étudiant et le code reçu.',
  attendanceSuccess: 'Votre présence a bien été enregistrée.',
  exerciseSuccess: 'Votre lien a été transmis pour relecture.',
  genericError: 'La demande n’a pas abouti. Vérifiez les informations puis réessayez.',
}

export default function EtudiantPage() {
  const [etudiantId, setEtudiantId] = useState('')
  const [code, setCode] = useState('')
  const [sessionId, setSessionId] = useState('')
  const [lien, setLien] = useState('')
  const [message, setMessage] = useState('')
  const [erreur, setErreur] = useState('')
  const [chargement, setChargement] = useState(false)

  async function executer(action) {
    setErreur('')
    setMessage('')
    if (!etudiantId) {
      setErreur(messages.missing)
      return
    }
    setChargement(true)
    try {
      await action()
    } catch (error) {
      const retryDelay = error.reessayerDansSecondes ? ` Réessayez dans ${error.reessayerDansSecondes} secondes.` : ''
      setErreur(`${error.message || messages.genericError}${retryDelay}`)
    } finally {
      setChargement(false)
    }
  }

  function marquerPresence(event) {
    event.preventDefault()
    executer(async () => {
      await api.marquerPresence({ code: code.trim().toUpperCase(), etudiantId: Number(etudiantId) })
      setMessage(messages.attendanceSuccess)
      setCode('')
    })
  }

  function deposerExercice(event) {
    event.preventDefault()
    executer(async () => {
      await api.deposerExercice({ sessionId: Number(sessionId), etudiantId: Number(etudiantId), lien: lien.trim() })
      setMessage(messages.exerciseSuccess)
      setLien('')
    })
  }

  return (
    <main className="mx-auto w-full max-w-5xl px-5 py-10 lg:px-8 lg:py-16">
      <section className="animate-fade-up max-w-2xl">
        <span className="eyebrow">Espace étudiant</span>
        <h1 className="mt-4 text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">Votre parcours, simplement suivi.</h1>
        <p className="mt-5 text-base leading-7 text-slate-600">Marquez votre présence en quelques secondes puis partagez le lien de votre exercice avec votre promotion.</p>
      </section>
      <label className="field-label mt-8 max-w-md" htmlFor="etudiantId">Identifiant étudiant
        <input className="field-input" id="etudiantId" type="number" min="1" value={etudiantId} onChange={event => setEtudiantId(event.target.value)} placeholder="Votre identifiant" required />
      </label>
      <div className="mt-7 grid gap-6 md:grid-cols-2">
        <form className="panel space-y-5 p-6 sm:p-8" onSubmit={marquerPresence}>
          <div><span className="step-badge">01</span><h2 className="mt-5 text-xl font-semibold text-slate-950">Marquer ma présence</h2><p className="mt-2 text-sm leading-6 text-slate-600">Saisissez le code affiché par votre formateur. Il reste valide pendant 15 minutes.</p></div>
          <label className="field-label" htmlFor="code">Code de présence
            <input className="field-input font-mono uppercase tracking-[0.2em]" id="code" value={code} onChange={event => setCode(event.target.value.toUpperCase())} placeholder="ABC234" required maxLength={12} />
          </label>
          <button className="button-primary w-full" disabled={chargement}>Valider ma présence</button>
        </form>
        <form className="panel space-y-5 p-6 sm:p-8" onSubmit={deposerExercice}>
          <div><span className="step-badge">02</span><h2 className="mt-5 text-xl font-semibold text-slate-950">Déposer mon exercice</h2><p className="mt-2 text-sm leading-6 text-slate-600">Le dépôt reste possible jusqu’à la clôture de la session.</p></div>
          <label className="field-label" htmlFor="sessionId">Identifiant de session
            <input className="field-input" id="sessionId" type="number" min="1" value={sessionId} onChange={event => setSessionId(event.target.value)} placeholder="Identifiant communiqué" required />
          </label>
          <label className="field-label" htmlFor="lien">Lien de l’exercice
            <input className="field-input" id="lien" type="url" value={lien} onChange={event => setLien(event.target.value)} placeholder="https://…" required maxLength={2048} />
          </label>
          <button className="button-secondary w-full" disabled={chargement}>Envoyer mon exercice</button>
        </form>
      </div>
      {erreur && <p role="alert" className="mt-6 rounded-xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{erreur}</p>}
      {message && <p role="status" className="mt-6 rounded-xl bg-teal-50 px-4 py-3 text-sm font-medium text-teal-800">{message}</p>}
    </main>
  )
}
