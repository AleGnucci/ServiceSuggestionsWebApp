import { NgModule } from '@angular/core';
import { CommonModule, } from '@angular/common';
import { BrowserModule  } from '@angular/platform-browser';
import { Routes, RouterModule } from '@angular/router';

import {GameSuggestionsComponent} from './game-suggestions/game-suggestions.component';
import { ComponentsComponent } from './components/components.component';
import {SuggestionComponent} from './game-suggestions/suggestion/suggestion.component';

const routes: Routes = [
    { path: '',                component: ComponentsComponent },
    { path: 'game/:gameId/suggestions',          component: GameSuggestionsComponent },
    { path: 'test',          component: SuggestionComponent },
];

@NgModule({
    imports: [
        CommonModule,
        BrowserModule,
        RouterModule.forRoot(routes)
    ],
    exports: [],
})
export class AppRoutingModule { }
