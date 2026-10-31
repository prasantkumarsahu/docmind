import { useState } from 'react'
import type { Source } from '../lib/types'

export default function SourceList({ sources }: { sources: Source[] }) {
  const [open, setOpen] = useState(false)
  if (sources.length === 0) return null

  return (
    <div className="mt-3 border-t border-slate-100 pt-2">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        className="text-xs font-medium text-indigo-600 hover:text-indigo-800"
      >
        {open ? 'Hide' : 'Show'} {sources.length} source{sources.length > 1 ? 's' : ''}
      </button>
      {open && (
        <ol className="mt-2 space-y-2">
          {sources.map((source) => (
            <li key={source.index} className="rounded-lg bg-slate-50 p-2 text-xs">
              <div className="flex items-center justify-between gap-2 font-medium text-slate-700">
                <span className="truncate">
                  [{source.index}] {source.filename}
                  {source.page != null && <span className="text-slate-400"> · page {source.page}</span>}
                </span>
                {source.score != null && (
                  <span className="shrink-0 text-slate-400">{Math.round(source.score * 100)}% match</span>
                )}
              </div>
              <p className="mt-1 text-slate-500">{source.snippet}</p>
            </li>
          ))}
        </ol>
      )}
    </div>
  )
}
