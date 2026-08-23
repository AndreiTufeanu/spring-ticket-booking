import { Component, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryDto } from '../../../../models/category.model';

@Component({
  selector: 'app-category-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './category-picker.html',
  styleUrl: './category-picker.css',
})
export class CategoryPicker {
  categories = input.required<CategoryDto[]>();
  selectedIds = input.required<string[]>();
  selectedIdsChange = output<string[]>();

  // Preserves the order categories were selected in (not the master list order)
  selectedCategories = computed(() => {
    const byId = new Map(this.categories().map(c => [c.id, c]));
    return this.selectedIds()
      .map(id => byId.get(id))
      .filter((c): c is CategoryDto => !!c);
  });

  isSelected(id: string): boolean {
    return this.selectedIds().includes(id);
  }

  toggleCategory(id: string) {
    const current = this.selectedIds();
    const updated = current.includes(id)
      ? current.filter(x => x !== id)
      : [...current, id];
    this.selectedIdsChange.emit(updated);
  }

  removeCategory(id: string) {
    this.selectedIdsChange.emit(this.selectedIds().filter(x => x !== id));
  }
}