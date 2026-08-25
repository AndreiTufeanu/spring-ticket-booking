import { Component, signal, ElementRef, viewChild, effect, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatMessage } from '../../../models/chat.model';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-ai-helper',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-helper.html',
  styleUrl: './ai-helper.css',
})
export class AiHelper implements OnInit {
  private readonly api = inject(ApiService);
  private readonly messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');
  private readonly chatInput = viewChild<ElementRef<HTMLTextAreaElement>>('chatInput');

  messages = signal<ChatMessage[]>([]);
  draftMessage = signal<string>('');
  sending = signal<boolean>(false);

  constructor() {
    effect(() => {
      this.messages();
      this.sending();
      queueMicrotask(() => this.scrollToBottom());
    });
  }

  ngOnInit() {
    this.api.getChatMessages().subscribe({
      next: (msgs) => this.messages.set(msgs),
      error: (err) => console.error('Failed to load chat history', err),
    });
  }

  sendMessage() {
    const content = this.draftMessage().trim();
    if (!content || this.sending()) return;

    this.messages.update(msgs => [
      ...msgs,
      { id: crypto.randomUUID(), role: 'user', content }
    ]);
    this.draftMessage.set('');

    const textarea = this.chatInput()?.nativeElement;
    if (textarea) textarea.style.height = 'auto';

    this.sending.set(true);
    this.api.sendChatMessage(content).subscribe({
      next: (assistantMessage) => {
        this.messages.update(msgs => [...msgs, assistantMessage]);
        this.sending.set(false);
      },
      error: () => {
        this.messages.update(msgs => [
          ...msgs,
          { id: crypto.randomUUID(), role: 'assistant', content: 'Something went wrong — try again in a moment.' }
        ]);
        this.sending.set(false);
      },
    });
  }

  handleKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  autoResize(textarea: HTMLTextAreaElement) {
    const maxHeight = 200;
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, maxHeight) + 'px';
  }

  private scrollToBottom() {
    this.messagesEnd()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }
}