import { Routes } from '@angular/router';
import { ChatComponent } from './pages/chat/chat.component';
import { CricketComponent } from './pages/cricket/cricket.component';
import { LoginComponent } from './pages/login/login.component';
import { OAuthCallbackComponent } from './pages/oauth-callback/oauth-callback.component';
import { AuthGuard } from './auth/auth.guard';
import { AdminGuard } from './auth/admin.guard';
import { SuperAdminGuard } from './auth/super-admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'oauth2/callback',
    component: OAuthCallbackComponent
  },
  {
    path: 'chat',
    component: ChatComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'cricket',
    component: CricketComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'dashboard',
    redirectTo: '/chat',
    pathMatch: 'full'
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },

{
   path: 'super-admin',
  component: SuperAdminDashboardComponent,
  canActivate: [SuperAdminGuard]
}

];
