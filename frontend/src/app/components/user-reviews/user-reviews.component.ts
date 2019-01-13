import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';

@Component({
  selector: 'app-my-reviews',
  templateUrl: './user-reviews.component.html',
  styleUrls: ['../../shared/css/card-list.scss']
})
export class UserReviewsComponent implements OnInit {

  reviews: Array<Review> = [];
  searchCompleted = false;
  itemType = this.getItemType();
  isForCurrentUser = this.httpUtilities.getUrlPart(2).includes('current');
  userId = this.isForCurrentUser ? 0 : +this.httpUtilities.getUrlPart(4);
  userName = "";

  constructor(private httpUtilities: HttpUtilitiesService) { }

  ngOnInit() {
    if(this.isForCurrentUser){
        this.httpUtilities.getReviewsByUser(this.itemType, this.reviews,
            () => this.searchCompleted = true);
    } else {
        this.httpUtilities.getUserName(this.userId, userName => this.userName = userName);
        this.httpUtilities.getReviewsByUser(this.itemType, this.reviews,
            () => this.searchCompleted = true, this.userId);
    }
  }

  getItemType(){
    const pluralItemType = this.httpUtilities.getUrlPart(2).split('_')[0];
    return this.isForCurrentUser ? pluralItemType.substring(0, pluralItemType.length - 1) : pluralItemType
  }

}
