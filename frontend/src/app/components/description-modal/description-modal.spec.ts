import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescriptionModal } from './description-modal';

describe('DescriptionModal', () => {
  let component: DescriptionModal;
  let fixture: ComponentFixture<DescriptionModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DescriptionModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DescriptionModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
