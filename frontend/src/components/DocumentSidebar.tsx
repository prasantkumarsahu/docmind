import { useRef, useState } from 'react'
import type { StoredDocument } from '../lib/types'

interface Props {
  documents: StoredDocument[]
  selectedIds: string[]
  uploading: boolean
  onUpload: (files: File[]) => void
  onToggle: (id: string) => void
  onDelete: (id: string) => void
}

const ACCEPT = '.pdf,.docx,.txt,.md,.html'

function formatSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function StatusBadge({ document }: { document: StoredDocument }) {
  if (document.status === 'READY') {
    return <span className="text-xs text-emerald-600">{document.chunkCount} chunks</span>
  }
  if (document.status === 'FAILED') {
    return (
      <span className="text-xs text-rose-600" title={document.error ?? undefined}>
        Failed
      </span>
    )
  }
  return <span className="animate-pulse text-xs text-amber-600">Processing…</span>
}

export default function DocumentSidebar({ documents, selectedIds, uploading, onUpload, onToggle, onDelete }: Props) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [dragging, setDragging] = useState(false)

  const handleFiles = (files: FileList | null) => {
    if (files && files.length > 0) onUpload(Array.from(files))
  }

  return (
    <aside className="flex h-full w-full flex-col border-r border-slate-200 bg-white md:w-80">
      <div className="border-b border-slate-200 px-5 py-4">
        <div className="flex items-center gap-2">
          <img src="/favicon.svg" alt="" className="h-7 w-7" />
          <h1 className="text-lg font-semibold text-slate-900">DocMind</h1>
        </div>
        <p className="mt-1 text-sm text-slate-500">Chat with your documents</p>
      </div>

      <div className="p-4">
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          onDragOver={(event) => {
            event.preventDefault()
            setDragging(true)
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={(event) => {
            event.preventDefault()
            setDragging(false)
            handleFiles(event.dataTransfer.files)
          }}
          className={`w-full rounded-xl border-2 border-dashed px-4 py-6 text-center text-sm transition ${
            dragging ? 'border-indigo-500 bg-indigo-50' : 'border-slate-300 hover:border-indigo-400 hover:bg-slate-50'
          }`}
        >
          <span className="block font-medium text-slate-700">{uploading ? 'Uploading…' : 'Upload documents'}</span>
          <span className="mt-1 block text-xs text-slate-500">PDF, DOCX, TXT, MD or HTML · up to 25 MB</span>
        </button>
        <input
          ref={inputRef}
          type="file"
          accept={ACCEPT}
          multiple
          hidden
          onChange={(event) => {
            handleFiles(event.target.files)
            event.target.value = ''
          }}
        />
      </div>

      <div className="flex items-center justify-between px-5 pb-2 text-xs font-medium uppercase tracking-wide text-slate-500">
        <span>Documents ({documents.length})</span>
        <span>{selectedIds.length === 0 ? 'Searching all' : `${selectedIds.length} selected`}</span>
      </div>

      <ul className="flex-1 space-y-1 overflow-y-auto px-3 pb-4">
        {documents.length === 0 && (
          <li className="px-2 py-6 text-center text-sm text-slate-400">No documents yet. Upload one to get started.</li>
        )}
        {documents.map((document) => {
          const selected = selectedIds.includes(document.id)
          return (
            <li
              key={document.id}
              className={`group flex items-start gap-3 rounded-lg px-2 py-2 ${selected ? 'bg-indigo-50' : 'hover:bg-slate-50'}`}
            >
              <input
                type="checkbox"
                className="mt-1 h-4 w-4 accent-indigo-600"
                checked={selected}
                disabled={document.status !== 'READY'}
                onChange={() => onToggle(document.id)}
                aria-label={`Use ${document.filename} only`}
              />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-slate-800" title={document.filename}>
                  {document.filename}
                </p>
                <div className="flex gap-2 text-xs text-slate-400">
                  <span>{formatSize(document.sizeBytes)}</span>
                  <StatusBadge document={document} />
                </div>
              </div>
              <button
                type="button"
                onClick={() => onDelete(document.id)}
                className="rounded p-1 text-slate-400 opacity-0 transition group-hover:opacity-100 hover:bg-rose-50 hover:text-rose-600 focus:opacity-100"
                aria-label={`Delete ${document.filename}`}
              >
                <svg viewBox="0 0 20 20" fill="currentColor" className="h-4 w-4">
                  <path
                    fillRule="evenodd"
                    d="M8.75 1A2.75 2.75 0 0 0 6 3.75v.443c-.795.077-1.584.176-2.365.298a.75.75 0 1 0 .23 1.482l.149-.022.841 10.518A2.75 2.75 0 0 0 7.596 19h4.807a2.75 2.75 0 0 0 2.742-2.53l.841-10.52.149.023a.75.75 0 0 0 .23-1.482A41 41 0 0 0 14 4.193V3.75A2.75 2.75 0 0 0 11.25 1h-2.5ZM10 4c.84 0 1.673.025 2.5.075V3.75c0-.69-.56-1.25-1.25-1.25h-2.5c-.69 0-1.25.56-1.25 1.25v.325C8.327 4.025 9.16 4 10 4Z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>
            </li>
          )
        })}
      </ul>
    </aside>
  )
}
