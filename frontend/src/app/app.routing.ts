import { NgModule } from '@angular/core';
import { CommonModule, } from '@angular/common';
import { BrowserModule  } from '@angular/platform-browser';
import { Routes, RouterModule } from '@angular/router';

import { ComponentsComponent } from './components/components.component';
import {LoginComponent} from './components/login/login.component';
import {RegisterComponent} from './components/register/register.component';
import {RecommendationsComponent} from './components/recommendations/recommendations.component';
import {ItemReviewsComponent} from './components/item-reviews/item-reviews.component';
import {AddReviewComponent} from './components/add-review/add-review.component';
import {DeleteAccountComponent} from './components/delete-account/delete-account.component';
import {FindItemComponent} from './components/find-item/find-item.component';
import {AddServiceComponent} from './components/add-service/add-service.component';
import {ServiceInfoComponent} from './components/service-info/service-info.component';
import {StarRatingModule} from 'angular-star-rating';
import {UserReviewsComponent} from './components/user-reviews/user-reviews.component';
import {LoginActivate} from './shared/utilities/LoginActivate';

const routes: Routes = [
    { path: '', component: ComponentsComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'unregister', component: DeleteAccountComponent, canActivate: [LoginActivate] },
    { path: 'recommendations/service_category/:service_category', component: RecommendationsComponent, canActivate: [LoginActivate]},
    { path: 'reviews/service/:service_id', component: ItemReviewsComponent, canActivate: [LoginActivate] },
    { path: 'reviews/place/:place_id', component: ItemReviewsComponent, canActivate: [LoginActivate] },
    { path: 'reviews/service/:item_id/add', component: AddReviewComponent, canActivate: [LoginActivate] },
    { path: 'reviews/place/:item_id/add', component: AddReviewComponent, canActivate: [LoginActivate] },
    { path: 'service/add', component: AddServiceComponent, canActivate: [LoginActivate] },
    { path: 'place/add', component: AddServiceComponent, canActivate: [LoginActivate] },
    { path: 'service/find', component: FindItemComponent, canActivate: [LoginActivate] },
    { path: 'place/find', component: FindItemComponent, canActivate: [LoginActivate] },
    { path: 'info/service/:service_id', component: ServiceInfoComponent, canActivate: [LoginActivate] },
    { path: 'reviews/services_by_current_user', component: UserReviewsComponent, canActivate: [LoginActivate] },
    { path: 'reviews/places_by_current_user', component: UserReviewsComponent, canActivate: [LoginActivate] },
    { path: 'reviews/service/by_user/:user_id', component: UserReviewsComponent, canActivate: [LoginActivate] },
    { path: 'reviews/place/by_user/:user_id', component: UserReviewsComponent, canActivate: [LoginActivate] },
    { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
    imports: [
        CommonModule,
        BrowserModule,
        RouterModule.forRoot(routes),
        StarRatingModule.forRoot()
    ],
    exports: [],
})
export class AppRoutingModule { }
