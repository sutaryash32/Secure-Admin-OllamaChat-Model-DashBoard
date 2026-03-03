import { Component } from '@angular/core';

import { sharedComponenet } from '../../utils/shared.component';

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

}
