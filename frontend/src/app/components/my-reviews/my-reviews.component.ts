import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';

@Component({
  selector: 'app-my-reviews',
  templateUrl: './my-reviews.component.html',
  styleUrls: ['../../shared/css/card-list.scss']
})
export class MyReviewsComponent implements OnInit {

  reviews: Array<Review> = [];
  searchCompleted = false;
  itemType = this.getItemType();

  constructor(private httpUtilities: HttpUtilitiesService) { }

  ngOnInit() {
    this.httpUtilities.getReviewsByCurrentUser(this.itemType, this.reviews,
        () => this.searchCompleted = true);
  }

  private getItemType(){
    const pluralItemType = this.httpUtilities.getUrlPart(2).split('_')[0]
    return pluralItemType.substring(0, pluralItemType.length - 1)
  }

}
