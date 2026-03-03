import { Component } from '@angular/core';

import { sharedComponenet } from '../../utils/shared.component';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-sidemenu',
  imports: [sharedComponenet],
  templateUrl: './sidemenu.component.html',
  styleUrl: './sidemenu.component.css'
})
export class SidemenuComponent {

  chatPath = '/chat';
  cricketPath = '/cricket';
  imagePath = '/image';

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }

}
