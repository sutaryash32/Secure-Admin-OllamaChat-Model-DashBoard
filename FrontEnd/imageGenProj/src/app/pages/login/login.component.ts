import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="login-box">
        <h2>{{ isRegisterMode ? 'Register' : 'Login' }}</h2>

        <form (ngSubmit)="onSubmit()">
          <div class="form-group" *ngIf="isRegisterMode">
            <input
              type="text"
              [(ngModel)]="username"
              name="username"
              placeholder="Username"
              required
              class="form-input">
          </div>

          <div class="form-group" *ngIf="isRegisterMode">
            <input
              type="email"
              [(ngModel)]="email"
              name="email"
              placeholder="Email"
              required
              class="form-input">
          </div>

          <div class="form-group">
            <input
              type="text"
              [(ngModel)]="loginUsername"
              name="loginUsername"
              placeholder="Username"
              required
              class="form-input"
              *ngIf="!isRegisterMode">
          </div>

          <div class="form-group">
            <input
              type="password"
              [(ngModel)]="password"
              name="password"
              placeholder="Password"
              required
              class="form-input">
          </div>

          <button type="submit" class="submit-btn" [disabled]="isLoading">
            {{ isLoading ? 'Processing...' : (isRegisterMode ? 'Register' : 'Login') }}
          </button>
        </form>

        <p class="toggle-link" (click)="toggleMode()">
          {{ isRegisterMode ? 'Already have an account? Login' : 'Need an account? Register' }}
        </p>

        <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .login-box {
      background: white;
      padding: 40px;
      border-radius: 10px;
      box-shadow: 0 10px 25px rgba(0,0,0,0.2);
      width: 100%;
      max-width: 400px;
    }

    h2 {
      text-align: center;
      color: #333;
      margin-bottom: 30px;
    }

    .form-group {
      margin-bottom: 20px;
    }

    .form-input {
      width: 100%;
      padding: 12px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
      box-sizing: border-box;
    }

    .form-input:focus {
      outline: none;
      border-color: #667eea;
    }

    .submit-btn {
      width: 100%;
      padding: 12px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 5px;
      font-size: 16px;
      cursor: pointer;
      transition: transform 0.2s;
    }

    .submit-btn:hover:not(:disabled) {
      transform: scale(1.02);
    }

    .submit-btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .toggle-link {
      text-align: center;
      margin-top: 20px;
      color: #667eea;
      cursor: pointer;
    }

    .toggle-link:hover {
      text-decoration: underline;
    }

    .error-message {
      color: red;
      text-align: center;
      margin-top: 15px;
    }
  `]
})
export class LoginComponent {
  isRegisterMode = false;
  username = '';
  email = '';
  loginUsername = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  private returnUrl = '/chat';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/chat';
  }

  toggleMode(): void {
    this.isRegisterMode = !this.isRegisterMode;
    this.errorMessage = '';
  }

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';

    if (this.isRegisterMode) {
      this.authService.register(this.username, this.email, this.password)
        .subscribe({
          next: () => {
            this.router.navigate([this.returnUrl]);
          },
          error: (err) => {
            this.errorMessage = err.error?.message || 'Registration failed';
            this.isLoading = false;
          }
        });
    } else {
      this.authService.login(this.loginUsername, this.password)
        .subscribe({
          next: () => {
            this.router.navigate([this.returnUrl]);
          },
          error: (err) => {
            this.errorMessage = err.error?.message || 'Invalid credentials';
            this.isLoading = false;
          }
        });
    }
  }
}
