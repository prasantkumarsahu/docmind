import { useEffect, useRef, useState } from 'react'
import Markdown from 'react-markdown'
import type { ChatMessage } from '../lib/types'
import SourceList from './SourceList'

interface Props {
  messages: ChatMessage[]
  busy: boolean
  hasDocuments: boolean
  onSend: (question: string) => void
  onStop: () => void
  onReset: () => void
}

const SUGGESTIONS = ['Summarize this document', 'What are the key points?', 'List any dates or deadlines mentioned']

export default function ChatPanel({ messages, busy, hasDocuments, onSend, onStop, onReset }: Props) {
  const [input, setInput] = useState('')
  const endRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const submit = (question: string) => {
    const trimmed = question.trim()
    if (!trimmed || busy) return
    onSend(trimmed)
    setInput('')
  }

  return (
    <section className="flex h-full flex-1 flex-col">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
        <h2 className="text-sm font-medium text-slate-600">Conversation</h2>
        <button
          type="button"
          onClick={onReset}
          disabled={messages.length === 0 || busy}
          className="rounded-lg px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100 disabled:opacity-40"
        >
          New chat
        </button>
      </header>

      <div className="flex-1 overflow-y-auto px-4 py-6 md:px-10">
        {messages.length === 0 ? (
          <div className="mx-auto mt-16 max-w-md text-center">
            <h3 className="text-xl font-semibold text-slate-800">Ask anything about your documents</h3>
            <p className="mt-2 text-sm text-slate-500">
              {hasDocuments
                ? 'Answers are grounded in your files and include the sources they came from.'
                : 'Upload a document on the left to get started.'}
            </p>
            {hasDocuments && (
              <div className="mt-6 flex flex-wrap justify-center gap-2">
                {SUGGESTIONS.map((suggestion) => (
                  <button
                    key={suggestion}
                    type="button"
                    onClick={() => submit(suggestion)}
                    className="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-600 hover:border-indigo-300 hover:text-indigo-700"
                  >
                    {suggestion}
                  </button>
                ))}
              </div>
            )}
          </div>
        ) : (
          <div className="mx-auto max-w-3xl space-y-5">
            {messages.map((message) => (
              <div key={message.id} className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed shadow-sm ${
                    message.role === 'user'
                      ? 'bg-indigo-600 text-white'
                      : message.error
                        ? 'border border-rose-200 bg-rose-50 text-rose-700'
                        : 'border border-slate-200 bg-white'
                  }`}
                >
                  {message.role === 'assistant' ? (
                    <>
                      {message.content ? (
                        <div className="prose-answer">
                          <Markdown>{message.content}</Markdown>
                        </div>
                      ) : (
                        <span className="inline-flex gap-1 py-1">
                          <span className="h-2 w-2 animate-bounce rounded-full bg-slate-300" />
                          <span className="h-2 w-2 animate-bounce rounded-full bg-slate-300 [animation-delay:150ms]" />
                          <span className="h-2 w-2 animate-bounce rounded-full bg-slate-300 [animation-delay:300ms]" />
                        </span>
                      )}
                      {!message.pending && message.sources && <SourceList sources={message.sources} />}
                    </>
                  ) : (
                    <p className="whitespace-pre-wrap">{message.content}</p>
                  )}
                </div>
              </div>
            ))}
            <div ref={endRef} />
          </div>
        )}
      </div>

      <form
        onSubmit={(event) => {
          event.preventDefault()
          submit(input)
        }}
        className="border-t border-slate-200 bg-white p-4"
      >
        <div className="mx-auto flex max-w-3xl items-end gap-2">
          <textarea
            value={input}
            onChange={(event) => setInput(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault()
                submit(input)
              }
            }}
            rows={1}
            maxLength={2000}
            placeholder={hasDocuments ? 'Ask a question…' : 'Upload a document first'}
            disabled={!hasDocuments}
            className="max-h-40 min-h-11 flex-1 resize-none rounded-xl border border-slate-300 px-4 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 disabled:bg-slate-50"
          />
          {busy ? (
            <button
              type="button"
              onClick={onStop}
              className="h-11 rounded-xl bg-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-300"
            >
              Stop
            </button>
          ) : (
            <button
              type="submit"
              disabled={!input.trim() || !hasDocuments}
              className="h-11 rounded-xl bg-indigo-600 px-5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-40"
            >
              Send
            </button>
          )}
        </div>
      </form>
    </section>
  )
}
