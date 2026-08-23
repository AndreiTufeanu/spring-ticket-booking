import { Component, signal, ElementRef, viewChild, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatMessage } from '../../models/chat.model';

@Component({
  selector: 'app-ai-helper',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-helper.html',
  styleUrl: './ai-helper.css',
})
export class AiHelper {
  private readonly messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');
  private readonly chatInput = viewChild<ElementRef<HTMLTextAreaElement>>('chatInput');

  messages = signal<ChatMessage[]>([]);
  draftMessage = signal<string>('');
  sending = signal<boolean>(false);

  constructor() {
    // Auto-scroll to the latest message whenever the list changes
    effect(() => {
      this.messages();
      this.sending();
      queueMicrotask(() => this.scrollToBottom());
    });
  }

  sendMessage() {
    const content = this.draftMessage().trim();
    if (!content || this.sending()) return;

    this.messages.update(msgs => [...msgs, { id: crypto.randomUUID(), role: 'user', content }]);
    this.draftMessage.set('');

    const textarea = this.chatInput()?.nativeElement;
    if (textarea) textarea.style.height = 'auto';

    // TODO: replace this block with a real API call once the backend endpoint exists.
    this.sending.set(true);
    setTimeout(() => {
      this.messages.update(msgs => [
        ...msgs,
        { id: crypto.randomUUID(), role: 'assistant', content: 'This is a placeholder response — hook me up to the backend to get real answers.' }
      ]);
      this.sending.set(false);
    }, 600);
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