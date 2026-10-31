export type DocumentStatus = 'PROCESSING' | 'READY' | 'FAILED'

export interface StoredDocument {
  id: string
  filename: string
  contentType: string | null
  sizeBytes: number
  chunkCount: number
  status: DocumentStatus
  error: string | null
  createdAt: string
}

export interface Source {
  index: number
  documentId: string
  filename: string
  page: number | null
  snippet: string
  score: number | null
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  sources?: Source[]
  pending?: boolean
  error?: boolean
}
