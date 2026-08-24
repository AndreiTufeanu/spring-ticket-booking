import { Component, input, output, effect, HostListener } from '@angular/core';

@Component({
  selector: 'app-description-modal',
  standalone: true,
  imports: [],
  templateUrl: './description-modal.html',
  styleUrl: './description-modal.css',
})
export class DescriptionModal {
  open = input.required<boolean>();
  eventTitle = input<string>('');
  eventDescription = input<string>('');
  closed = output<void>();

  constructor() {
    effect(() => {
      document.body.style.overflow = this.open() ? 'hidden' : '';
    });
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    if (this.open()) {
      this.close();
    }
  }

  close() {
    this.closed.emit();
  }
}