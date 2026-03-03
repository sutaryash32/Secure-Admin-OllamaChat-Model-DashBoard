import { Routes } from '@angular/router';
import { ChatComponent } from './pages/chat/chat.component';
import { CricketComponent } from './pages/cricket/cricket.component';

export const routes: Routes = [

  {
    path : 'chat' ,
    component : ChatComponent
  },

  {
    path : 'cricket',
    component : CricketComponent
  },
];
