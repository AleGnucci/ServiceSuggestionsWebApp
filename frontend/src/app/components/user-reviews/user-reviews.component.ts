import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Toast} from '../../shared/utilities/Toast';
import {StringUtilities} from '../../shared/utilities/StringUtilities';

@Component({
  selector: 'app-my-reviews',
  templateUrl: './user-reviews.component.html',
  styleUrls: ['../../shared/css/reviews.scss']
})
export class UserReviewsComponent implements OnInit {

  reviews: Array<Review> = [];
  searchCompleted = false;
  isForCurrentUser = this.httpUtilities.getUrlPart(2).includes('current');
  itemType = this.getItemType();
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

  deleteReview(itemId: number) {
    this.httpUtilities.deleteReview(this.reviews, itemId, () => {
      this.ngOnInit();
      Toast.toast('Review deleted')
    })
  }

  isCurrentUser(): boolean {
    return this.isForCurrentUser || this.userName === localStorage.getItem('userName');
  }

  getOtherItemType(): string {
    return this.itemType === 'service' ? 'place' : 'service'
  }

  getOtherReviewsLink(): string {
    const otherItemType = this.getOtherItemType();
    if(this.isForCurrentUser){
      return '/reviews/' + otherItemType + 's_by_current_user/';
    } else {
      return '/reviews/' + otherItemType + '/by_user/' + this.userId;
    }

  }

  getOtherReviewsTitle(): string {
    return StringUtilities.capitalizeFirstLetter(this.getOtherItemType()) + ' reviews from this user';
  }

}
