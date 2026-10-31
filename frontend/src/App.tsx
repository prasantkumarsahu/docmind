import { useCallback, useEffect, useRef, useState } from 'react'
import ChatPanel from './components/ChatPanel'
import DocumentSidebar from './components/DocumentSidebar'
import { clearConversation, deleteDocument, listDocuments, streamAnswer, uploadDocument } from './lib/api'
import type { ChatMessage, StoredDocument } from './lib/types'

const newId = () => crypto.randomUUID()

export default function App() {
  const [documents, setDocuments] = useState<StoredDocument[]>([])
  const [selectedIds, setSelectedIds] = useState<string[]>([])
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [uploading, setUploading] = useState(false)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<string | null>(null)
  const [conversationId, setConversationId] = useState(newId)
  const abortRef = useRef<AbortController | null>(null)

  const refresh = useCallback(
    () =>
      listDocuments()
        .then(setDocuments)
        .catch(() => setNotice('Cannot reach the DocMind API. Is the backend running on port 8080?')),
    [],
  )

  useEffect(() => {
    listDocuments()
      .then(setDocuments)
      .catch(() => setNotice('Cannot reach the DocMind API. Is the backend running on port 8080?'))
  }, [])

  useEffect(() => {
    if (!documents.some((document) => document.status === 'PROCESSING')) return
    const timer = setInterval(refresh, 2000)
    return () => clearInterval(timer)
  }, [documents, refresh])

  const updateLastMessage = (update: (message: ChatMessage) => ChatMessage) =>
    setMessages((current) => [...current.slice(0, -1), update(current[current.length - 1])])

  const handleUpload = async (files: File[]) => {
    setUploading(true)
    setNotice(null)
    for (const file of files) {
      try {
        await uploadDocument(file)
      } catch (error) {
        setNotice(`${file.name}: ${(error as Error).message}`)
      }
    }
    setUploading(false)
    refresh()
  }

  const handleDelete = async (id: string) => {
    try {
      await deleteDocument(id)
      setSelectedIds((ids) => ids.filter((selected) => selected !== id))
      refresh()
    } catch (error) {
      setNotice((error as Error).message)
    }
  }

  const handleSend = async (question: string) => {
    setBusy(true)
    setMessages((current) => [
      ...current,
      { id: newId(), role: 'user', content: question },
      { id: newId(), role: 'assistant', content: '', pending: true },
    ])

    const controller = new AbortController()
    abortRef.current = controller
    try {
      await streamAnswer(
        { conversationId, question, documentIds: selectedIds },
        {
          onSources: (sources) => updateLastMessage((message) => ({ ...message, sources })),
          onToken: (text) => updateLastMessage((message) => ({ ...message, content: message.content + text })),
          onError: (text) => updateLastMessage((message) => ({ ...message, content: text, error: true })),
        },
        controller.signal,
      )
    } catch (error) {
      if ((error as Error).name !== 'AbortError') {
        updateLastMessage((message) => ({ ...message, content: (error as Error).message, error: true }))
      }
    } finally {
      updateLastMessage((message) => ({ ...message, pending: false, content: message.content || 'Stopped.' }))
      setBusy(false)
      abortRef.current = null
    }
  }

  const handleReset = async () => {
    await clearConversation(conversationId)
    setMessages([])
    setConversationId(newId())
  }

  const toggle = (id: string) =>
    setSelectedIds((ids) => (ids.includes(id) ? ids.filter((selected) => selected !== id) : [...ids, id]))

  return (
    <div className="flex h-full flex-col md:flex-row">
      <div className="h-72 shrink-0 md:h-full">
        <DocumentSidebar
          documents={documents}
          selectedIds={selectedIds}
          uploading={uploading}
          onUpload={handleUpload}
          onToggle={toggle}
          onDelete={handleDelete}
        />
      </div>
      <main className="relative flex min-h-0 flex-1">
        {notice && (
          <div className="absolute left-1/2 top-16 z-10 flex -translate-x-1/2 items-center gap-3 rounded-lg bg-rose-600 px-4 py-2 text-sm text-white shadow-lg">
            <span>{notice}</span>
            <button type="button" onClick={() => setNotice(null)} className="font-semibold" aria-label="Dismiss">
              ×
            </button>
          </div>
        )}
        <ChatPanel
          messages={messages}
          busy={busy}
          hasDocuments={documents.some((document) => document.status === 'READY')}
          onSend={handleSend}
          onStop={() => abortRef.current?.abort()}
          onReset={handleReset}
        />
      </main>
    </div>
  )
}
