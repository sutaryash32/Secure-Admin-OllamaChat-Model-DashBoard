import { Component, OnDestroy, ViewChild, ElementRef, AfterViewChecked, NgZone } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { Message } from '../../model/message';
import { ApiService } from '../../service/api.service';

@Component({
  selector: 'app-cricket',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cricket.component.html',
  styleUrls: ['./cricket.component.css']
})
export class CricketComponent implements OnDestroy, AfterViewChecked {

  @ViewChild('scrollAnchor') private scrollAnchor!: ElementRef;

  inputTextPrompt = '';
  messageArray: Message[] = [];
  isStreaming = false;

  private subscription?: Subscription;
  private typewriterTimeouts: ReturnType<typeof setTimeout>[] = [];
  private totalChars = 0;

  constructor(private chatApi: ApiService, private ngZone: NgZone) {}

  askClicked(): void {
    const prompt = this.inputTextPrompt.trim();
    if (!prompt || this.isStreaming) return;

    this.messageArray.push(new Message(prompt, true));

    const aiMessage = new Message('', false);
    this.messageArray.push(aiMessage);

    this.inputTextPrompt = '';
    this.isStreaming = true;
    this.totalChars = 0;

    this.subscription?.unsubscribe();

    this.subscription = this.chatApi.getCricketStreamedResponse(prompt).subscribe({
      next: (token: string) => {
        this.ngZone.run(() => {
          const chars = token.split('');
          const baseDelay = this.totalChars * 5; // ✅ base offset for this token
          chars.forEach((char, i) => {
            const t = setTimeout(() => {
              aiMessage.text += char;
              this.messageArray = [...this.messageArray];
            }, baseDelay + i * 5); // ✅ small delay between chars within token
            this.totalChars++;
            this.typewriterTimeouts.push(t);
          });
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          console.error('Cricket streaming error:', err);
          aiMessage.text = aiMessage.text || 'Something went wrong. Please try again.';
          this.isStreaming = false;
          this.totalChars = 0;
        });
      },
      complete: () => {
        const maxDelay = this.totalChars * 5;
        setTimeout(() => {
          this.ngZone.run(() => {
            this.isStreaming = false;
            this.totalChars = 0;
          });
        }, maxDelay);
      }
    });
  }

  stopStreaming(): void {
    this.subscription?.unsubscribe();
    this.typewriterTimeouts.forEach(clearTimeout);
    this.typewriterTimeouts = [];
    this.totalChars = 0;
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
    this.typewriterTimeouts.forEach(clearTimeout);
    this.typewriterTimeouts = [];
    this.totalChars = 0;
  }


}
