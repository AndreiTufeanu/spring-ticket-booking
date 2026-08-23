import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { CategoryDto, CreateCategoryDto } from '../../../models/category.model';
import { NotificationService } from '../../../services/notification.service';
import { extractErrorMessage } from '../../../utils/api-error.util';

@Component({
  selector: 'app-categories-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories-management.html',
  styleUrl: './categories-management.css',
})
export class CategoriesManagement implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly notificationService = inject(NotificationService);

  categories = signal<CategoryDto[]>([]);
  loading = signal<boolean>(true);

  newCategory: CreateCategoryDto = { name: '' };

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.loading.set(true);
    this.apiService.getCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  createCategory() {
    if (!this.newCategory.name.trim()) return;

    this.apiService.createCategory(this.newCategory).subscribe({
      next: (category) => {
        this.categories.update(categories => [...categories, category]);
        this.newCategory = { name: '' };
        this.notificationService.showSuccess('Category created.');
      },
      error: (err) => {
        this.notificationService.showError(extractErrorMessage(err));
      }
    });
  }

  deleteCategory(id: string) {
    if (!confirm('Delete this category? It will be removed from any events using it.')) return;

    this.apiService.deleteCategory(id).subscribe({
      next: () => {
        this.categories.update(categories => categories.filter(c => c.id !== id));
        this.notificationService.showSuccess('Category deleted.');
      },
      error: (err) => {
        this.notificationService.showError(extractErrorMessage(err));
      }
    });
  }
}