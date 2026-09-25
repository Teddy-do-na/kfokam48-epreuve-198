import { BrowserRouter, NavLink, Navigate, Route, Routes } from 'react-router-dom'
import FormateurPage from './pages/FormateurPage.jsx'
import EtudiantPage from './pages/EtudiantPage.jsx'
import RelecteurPage from './pages/RelecteurPage.jsx'

const navigation = [
  { to: '/formateur', label: 'Formateur', short: '01' },
  { to: '/etudiant', label: 'Étudiant', short: '02' },
  { to: '/relecteur', label: 'Relecteur', short: '03' },
]

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-[#f5f8f7] text-slate-900">
        <header className="border-b border-slate-200/80 bg-white/90 backdrop-blur">
          <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-5 py-4 lg:px-8">
            <NavLink to="/formateur" className="flex items-center gap-3" aria-label="KFOKAM48, accueil">
              <span className="grid h-10 w-10 place-items-center rounded-xl bg-teal-700 text-sm font-bold tracking-tight text-white">K48</span>
              <span><span className="block text-sm font-semibold tracking-tight text-slate-950">KFOKAM48</span><span className="mt-0.5 block text-[11px] text-slate-500">Suivi pédagogique</span></span>
            </NavLink>
            <nav aria-label="Navigation principale" className="flex items-center gap-1 rounded-xl bg-slate-100 p-1">
              {navigation.map(item => <NavLink key={item.to} to={item.to} className={({ isActive }) => `rounded-lg px-3 py-2 text-xs font-semibold transition sm:px-4 sm:text-sm ${isActive ? 'bg-white text-teal-800 shadow-sm' : 'text-slate-500 hover:text-slate-900'}`}><span className="hidden sm:inline">{item.label}</span><span className="sm:hidden">{item.short}</span></NavLink>)}
            </nav>
          </div>
        </header>
        <Routes>
          <Route path="/" element={<Navigate to="/formateur" replace />} />
          <Route path="/formateur" element={<FormateurPage />} />
          <Route path="/etudiant" element={<EtudiantPage />} />
          <Route path="/relecteur" element={<RelecteurPage />} />
          <Route path="*" element={<Navigate to="/formateur" replace />} />
        </Routes>
        <footer className="mx-auto max-w-7xl px-5 pb-7 pt-3 text-xs text-slate-400 lg:px-8">KFOKAM48 <span className="px-2">·</span> Présence et relecture par les pairs</footer>
      </div>
    </BrowserRouter>
  )
}
