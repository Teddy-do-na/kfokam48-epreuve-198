export default function RelecteurPage() {
  return (
    <main className="mx-auto w-full max-w-5xl px-5 py-10 lg:px-8 lg:py-16">
      <section className="animate-fade-up max-w-2xl">
        <span className="eyebrow">Espace relecteur</span>
        <h1 className="mt-4 text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">Un regard neuf sur le travail d’un pair.</h1>
        <p className="mt-5 text-base leading-7 text-slate-600">Après votre présence, le système peut vous attribuer un exercice à relire. L’identité de l’auteur reste masquée.</p>
      </section>
      <section className="panel mt-8 max-w-2xl p-6 sm:p-8" aria-live="polite">
        <span className="step-badge">✓</span>
        <h2 className="mt-5 text-xl font-semibold text-slate-950">Aucune relecture attribuée pour le moment</h2>
        <p className="mt-3 text-sm leading-6 text-slate-600">Les exercices sont confiés à des étudiants présents, à l’exclusion de leur auteur. Si aucun pair n’est encore disponible, l’attribution sera réessayée lors d’une nouvelle présence.</p>
      </section>
    </main>
  )
}
