import { Injectable, inject } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class SuperAdminGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(): boolean {
    if (this.authService.isAuthenticated() && this.authService.isSuperAdmin()) {
      return true;
    }
    this.router.navigate(['/chat']);
    return false;
  }
}
