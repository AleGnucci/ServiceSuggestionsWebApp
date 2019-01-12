import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Router} from '@angular/router';
import {Constants} from '../../shared/constants/Constants';
import {Location} from '@angular/common';
import {Toast} from '../../shared/utilities/Toast';
import {CoordinatesEncoderDecoderService} from '../../shared/services/coordinates-encoder-decoder.service';

@Component({
  selector: 'app-add-review',
  templateUrl: './add-review.component.html',
  styleUrls: ['./add-review.component.scss']
})
export class AddReviewComponent implements OnInit {

  item = {name: ''};
  isServiceReview = false;
  stars = 0;

  constructor(private httpUtilities: HttpUtilitiesService, private router: Router, private _location: Location,
              private coordEncoderDecoder: CoordinatesEncoderDecoderService) { }

  ngOnInit() {
      this.isServiceReview = this.router.url.split('/')[2].includes('service');
      const itemId: number = Number.parseInt(this.httpUtilities.getUrlPart(3));
      if (this.isServiceReview) {
          this.httpUtilities.httpGet(Constants.restServerHost + '/service/' + itemId,
                  res => this.item = res.item);
      } else {
          this.coordEncoderDecoder.getNodeById(itemId, res => this.item = {name: res.display_name})
      }
  }

  addReview(commentText: string) {
    const starsAmount = this.stars;
    const body = {stars: starsAmount, comment: commentText};
    if (commentText !== '' && starsAmount > 0) {
        this.httpUtilities.httpPost(this.getReviewUrl(this.isServiceReview), body, () => {
          this._location.back();
          Toast.toast('Review added successfully')
        });
    } else {
      Toast.toast('Some data in the form is empty, try again')
    }
  }

  /*
  private getStars() {
    if (this.getInput('star5').checked) {
      return 5
    } else if (this.getInput('star4').checked) {
      return 4
    } else if (this.getInput('star3').checked) {
      return 3
    } else if (this.getInput('star2').checked) {
      return 2
    } else if (this.getInput('star1').checked) {
      return 1
    } else {
      return 0
    }
  }

  private getInput(name: string): HTMLInputElement {
      return <HTMLInputElement>document.getElementById(name)
  }
  */

  changeRating(event: any) {
      this.stars = event.rating;
  }

  private getReviewUrl(isServiceReview: boolean): string {
    let url = Constants.restServerHost + '/private';
    if (isServiceReview) {
      url += '/service/'
    } else {
      url += '/place/'
    }
    return url + this.httpUtilities.getUrlPart(3) + '/review'
  }

}
