import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {MapHelper} from '../../shared/utilities/MapHelper';
import {CoordinatesEncoderDecoderService} from '../../shared/services/coordinates-encoder-decoder.service';
import {Constants} from '../../shared/constants/Constants';
import {Toast} from '../../shared/utilities/Toast';
import {StringUtilities} from '../../shared/utilities/StringUtilities';

@Component({
  selector: 'app-place-reviews',
  templateUrl: './item-reviews.component.html',
  styleUrls: ['../../shared/css/reviews.scss']
})
export class ItemReviewsComponent implements OnInit {

    reviews: Array<Review> = [];
    userName = localStorage.getItem('userName');
    isServiceReviewsPage;
    itemType;
    place: PlaceItemWithDescription;
    service: ServiceItem;
    stars = 3;
    completedSetup = false;

  constructor(private httpUtilities: HttpUtilitiesService, private coordEncoderDecoder: CoordinatesEncoderDecoderService) {
  }

  ngOnInit() {
      this.isServiceReviewsPage = this.httpUtilities.getUrlPart(2) === 'service';
      this.itemType = this.isServiceReviewsPage ? 'service' : 'place';
      this.httpUtilities.getReviews(this.itemType, this.reviews);
      if (this.isServiceReviewsPage) {
          this.httpUtilities.getService(res => {
              this.service = res.item;
              this.completeSetup(res.stars)
          });
      } else {
        this.initPlace();
      }
  }

  private initPlace() {
      this.coordEncoderDecoder.getNodeById(Number.parseInt(this.httpUtilities.getUrlPart(3)), res => {
          this.place = {id: res.osm_id, location: [res.lat, res.lon], description: ''};
          this.coordEncoderDecoder.decode(this.place.location[0], this.place.location[1],
              placeDescription => this.place.description = placeDescription);
          this.httpUtilities.httpGet(Constants.restServerHost + '/place/' + res.osm_id + '/average_stars', stars => {
              this.completeSetup(stars.averageStars);
              MapHelper.showIFrame(this.place.location);
          });
      });
  }

  private completeSetup(stars: number) {
      if (stars !== null) {
          this.stars = stars;
      }
      this.completedSetup = true;
  }

    deleteReview() {
        this.httpUtilities.deleteReview(this.reviews, Number.parseInt(this.httpUtilities.getUrlPart(3)),
            () => {
            this.ngOnInit();
            Toast.toast('Review deleted')
        })
    }

    reviewsContainUserReview() {
      return this.reviews.find(review => review.userName === this.userName) !== undefined;
    }

    capitalize(string: string) {
        return StringUtilities.capitalizeFirstLetter(string)
    }
}
