import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { ChatbotComponent } from './chatbot';

describe('ChatbotComponent', () => {
  let component: ChatbotComponent;
  let fixture: ComponentFixture<ChatbotComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatbotComponent, FormsModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChatbotComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle chat window', () => {
    expect(component.isOpen).toBeFalsy();
    component.toggleChat();
    expect(component.isOpen).toBeTruthy();
    component.toggleChat();
    expect(component.isOpen).toBeFalsy();
  });

  it('should send message', () => {
    component.currentMessage = 'Test message';
    const initialMessageCount = component.messages.length;
    component.sendMessage();
    expect(component.messages.length).toBe(initialMessageCount + 1);
    expect(component.currentMessage).toBe('');
  });

  it('should have welcome message on init', () => {
    expect(component.messages.length).toBeGreaterThan(0);
    expect(component.messages[0].isUser).toBeFalsy();
  });
});