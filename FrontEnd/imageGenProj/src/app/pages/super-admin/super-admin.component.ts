import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../service/api.service';

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  isActive: boolean;
  createdAt: string;
  lastLogin: string;
}

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './super-admin.component.html',
  styleUrl: './super-admin.component.css'
})

export class SuperAdminDashboardComponent implements OnInit {

  users: User[] = [];
  analytics: any = null;
  errorMsg = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadAnalytics();
  }

  loadUsers(): void {
    this.api.getAllUsers().subscribe({
      next: (data: User[]) => this.users = data,
      error: () => this.errorMsg = 'Failed to load users.'
    });
  }

  loadAnalytics(): void {
    this.api.getChatAnalytics().subscribe({
      next: (data: any) => this.analytics = data,
      error: () => {}
    });
  }

  assignRole(user: User, role: string): void {
    if (!confirm(`Change ${user.name}'s role to ${role}?`)) return;
    this.api.updateUserRole(user.id, role).subscribe({
      next: (updated: User) => {
        user.role = updated.role;
        this.users = [...this.users];
        this.loadAnalytics();
      },
      error: () => this.errorMsg = 'Failed to update role.'
    });
  }

  disableUser(user: User): void {
    if (!confirm(`Disable ${user.name}?`)) return;
    this.api.disableUser(user.id).subscribe({
      next: (updated: User) => {
        user.isActive = updated.isActive;
        this.users = [...this.users];
        this.loadAnalytics();
      },
      error: () => this.errorMsg = 'Failed to disable user.'
    });
  }

  enableUser(user: User): void {
    if (!confirm(`Enable ${user.name}?`)) return;
    this.api.enableUser(user.id).subscribe({
      next: (updated: User) => {
        user.isActive = updated.isActive;
        this.users = [...this.users];
        this.loadAnalytics();
      },
      error: () => this.errorMsg = 'Failed to enable user.'
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Permanently delete ${user.name}? This cannot be undone.`)) return;
    this.api.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== user.id);
        this.loadAnalytics();
      },
      error: () => this.errorMsg = 'Failed to delete user.'
    });
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ROLE_SUPER_ADMIN' ? 'badge-super-admin' : 'badge-user';
  }
}
