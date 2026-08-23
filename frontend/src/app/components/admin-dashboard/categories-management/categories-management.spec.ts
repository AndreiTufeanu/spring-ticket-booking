import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoriesManagement } from './categories-management';

describe('CategoriesManagement', () => {
  let component: CategoriesManagement;
  let fixture: ComponentFixture<CategoriesManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoriesManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoriesManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
