import type { Source, StoredDocument } from './types'

async function parseError(response: Response): Promise<Error> {
  try {
    const body = await response.json()
    return new Error(body.detail ?? response.statusText)
  } catch {
    return new Error(response.statusText || 'Request failed')
  }
}

export async function listDocuments(): Promise<StoredDocument[]> {
  const response = await fetch('/api/documents')
  if (!response.ok) throw await parseError(response)
  return response.json()
}

export async function uploadDocument(file: File): Promise<StoredDocument> {
  const form = new FormData()
  form.append('file', file)
  const response = await fetch('/api/documents', { method: 'POST', body: form })
  if (!response.ok) throw await parseError(response)
  return response.json()
}

export async function deleteDocument(id: string): Promise<void> {
  const response = await fetch(`/api/documents/${id}`, { method: 'DELETE' })
  if (!response.ok) throw await parseError(response)
}

export async function clearConversation(conversationId: string): Promise<void> {
  await fetch(`/api/chat/${conversationId}`, { method: 'DELETE' })
}

export interface StreamHandlers {
  onSources: (sources: Source[]) => void
  onToken: (text: string) => void
  onError: (message: string) => void
}

export async function streamAnswer(
  body: { conversationId: string; question: string; documentIds: string[] },
  handlers: StreamHandlers,
  signal?: AbortSignal,
): Promise<void> {
  const response = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'text/event-stream' },
    body: JSON.stringify(body),
    signal,
  })
  if (!response.ok || !response.body) throw await parseError(response)

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    let boundary = buffer.indexOf('\n\n')
    while (boundary !== -1) {
      handleEvent(buffer.slice(0, boundary), handlers)
      buffer = buffer.slice(boundary + 2)
      boundary = buffer.indexOf('\n\n')
    }
  }
}

function handleEvent(raw: string, handlers: StreamHandlers) {
  let event = 'message'
  const data: string[] = []
  for (const line of raw.split('\n')) {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    else if (line.startsWith('data:')) data.push(line.slice(5))
  }
  if (data.length === 0) return
  const payload = JSON.parse(data.join('\n'))

  if (event === 'sources') handlers.onSources(payload)
  else if (event === 'token') handlers.onToken(payload.text)
  else if (event === 'error') handlers.onError(payload.message)
}
