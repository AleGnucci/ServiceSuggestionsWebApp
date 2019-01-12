import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameSuggestionsComponent } from './game-suggestions.component';

describe('GameSuggestionsComponent', () => {
  let component: GameSuggestionsComponent;
  let fixture: ComponentFixture<GameSuggestionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameSuggestionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameSuggestionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
