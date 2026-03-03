import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  loginWithGoogle(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Redirect to backend OAuth2 authorization endpoint
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  }
}
