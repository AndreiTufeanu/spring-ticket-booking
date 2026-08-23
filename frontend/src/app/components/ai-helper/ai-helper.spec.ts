import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiHelper } from './ai-helper';

describe('AiHelper', () => {
  let component: AiHelper;
  let fixture: ComponentFixture<AiHelper>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AiHelper]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiHelper);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
