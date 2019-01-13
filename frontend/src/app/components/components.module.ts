import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { NouisliderModule } from 'ng2-nouislider';
import { JWBootstrapSwitchModule } from 'jw-bootstrap-switch-ng2';
import { RouterModule } from '@angular/router';
import { ComponentsComponent } from './components.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { RecommendationsComponent } from './recommendations/recommendations.component';
import { ItemReviewsComponent } from './item-reviews/item-reviews.component';
import { AddReviewComponent } from './add-review/add-review.component';
import { DeleteAccountComponent } from './delete-account/delete-account.component';
import { FindItemComponent } from './find-item/find-item.component';
import { StarsComponent } from './stars/stars.component';
import {AddServiceComponent} from './add-service/add-service.component';
import { ServiceInfoComponent } from './service-info/service-info.component';
import {StarRatingModule} from 'angular-star-rating';
import { UserReviewsComponent } from './user-reviews/user-reviews.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        NgbModule,
        NouisliderModule,
        RouterModule,
        JWBootstrapSwitchModule,
        StarRatingModule.forRoot()
      ],
    declarations: [
        ComponentsComponent,
        LoginComponent,
        RegisterComponent,
        RecommendationsComponent,
        ItemReviewsComponent,
        AddReviewComponent,
        AddServiceComponent,
        DeleteAccountComponent,
        FindItemComponent,
        StarsComponent,
        ServiceInfoComponent,
        UserReviewsComponent
    ],
    exports: [ ComponentsComponent ]
})
export class ComponentsModule { }
