import { Component, OnDestroy, ElementRef, ViewChild, AfterViewChecked, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ApiService } from '../../service/api.service';
import { Message } from '../../model/message';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnDestroy, AfterViewChecked {

  @ViewChild('scrollAnchor') private scrollAnchor!: ElementRef;

  inputTextPrompt = '';
  messageArray: Message[] = [];
  isStreaming = false;

  private subscription?: Subscription;

  constructor(private api: ApiService, private ngZone: NgZone) {}

  askClicked(): void {
    const prompt = this.inputTextPrompt.trim();
    if (!prompt || this.isStreaming) return;

    this.messageArray.push(new Message(prompt, true));

    const aiMessage = new Message('', false);
    this.messageArray.push(aiMessage);

    this.inputTextPrompt = '';
    this.isStreaming = true;

    this.subscription?.unsubscribe();

    this.subscription = this.api.getStreamedResponse(prompt).subscribe({
      next: (token: string) => {
        this.ngZone.run(() => {
          aiMessage.text += token; // ✅ full token at once
          this.messageArray = [...this.messageArray];
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          console.error('Streaming error:', err);
          aiMessage.text = aiMessage.text || 'Something went wrong. Please try again.';
          this.isStreaming = false;
        });
      },
      complete: () => {
        this.ngZone.run(() => {
          this.isStreaming = false;
        });
      }
    });
  }

  stopStreaming(): void {
    this.subscription?.unsubscribe();
    this.isStreaming = false;
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.askClicked();
    }
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      this.scrollAnchor.nativeElement.scrollIntoView({ behavior: 'smooth' });
    } catch {}
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
