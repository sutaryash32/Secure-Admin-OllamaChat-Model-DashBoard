import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatComponent } from './pages/chat/chat.component';
import { CricketComponent } from './pages/cricket/cricket.component';
import { SidemenuComponent } from "./components/sidemenu/sidemenu.component";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SidemenuComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'AskAi';
}
