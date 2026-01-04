import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../services/chatbot.service';

declare const feather: any;

interface ChatMessage {
  id: number;
  text: string;
  isUser: boolean;
  timestamp: Date;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.html',
  styleUrls: ['./chatbot.css']
})
export class ChatbotComponent implements OnInit, AfterViewInit {
  isOpen = false;
  messages: ChatMessage[] = [];
  currentMessage = '';
  isTyping = false;
  messageIdCounter = 1;

  constructor(private chatbotService: ChatbotService) { }

  ngOnInit() {
    // Message de bienvenue
    this.addBotMessage("Bonjour ! Je suis votre assistant virtuel pour 9awi Niveau. Comment puis-je vous aider dans votre apprentissage aujourd'hui ?");
  }

  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      setTimeout(() => feather.replace(), 100);
    }
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      setTimeout(() => {
        this.scrollToBottom();
        if (typeof feather !== 'undefined') {
          feather.replace();
        }
      }, 100);
    }
  }

  sendMessage() {
    if (!this.currentMessage.trim()) return;

    // Ajouter le message de l'utilisateur
    const userMessage = this.currentMessage;
    this.addUserMessage(userMessage);
    this.currentMessage = '';

    // Réponse du bot via backend
    this.isTyping = true;
    this.chatbotService.sendMessage(userMessage).subscribe({
      next: (response) => {
        this.addBotMessage(response.response);
        this.isTyping = false;
      },
      error: (error) => {
        console.error('Erreur chatbot:', error);
        this.addBotMessage("Désolé, j'ai rencontré une petite erreur. Pourriez-vous réessayer ?");
        this.isTyping = false;
      }
    });
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private addUserMessage(text: string) {
    this.messages.push({
      id: this.messageIdCounter++,
      text: text,
      isUser: true,
      timestamp: new Date()
    });
    this.scrollToBottom();
  }

  private addBotMessage(text: string) {
    this.messages.push({
      id: this.messageIdCounter++,
      text: text,
      isUser: false,
      timestamp: new Date()
    });
    this.scrollToBottom();
  }

  private scrollToBottom() {
    setTimeout(() => {
      const chatMessages = document.querySelector('.chat-messages');
      if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
      }
    }, 100);
  }

  clearChat() {
    this.messages = [];
    this.addBotMessage("Chat effacé ! Comment puis-je vous aider ?");
  }
}
