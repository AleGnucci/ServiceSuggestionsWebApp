import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Router} from '@angular/router';
import {Observable} from 'rxjs';
import {Constants} from '../constants/Constants';
import {Toast} from '../utilities/Toast';

@Injectable({
  providedIn: 'root'
})
export class HttpUtilitiesService {

  constructor(private http: HttpClient, private router: Router) { }

  getLastUrlPart(): string {
      return this.getUrlPart(null, true)
  }

  getUrlPart(partIndex: number, getLast?: boolean): string {
      const urlParts = this.router.url.split('/');
      if (getLast) {
          return urlParts[urlParts.length - 1].trim()
      }
      return urlParts[partIndex].trim()
  }

  httpGet<T>(url: string, action: (T) => void, credentials?: boolean) {
      const withCredentials = credentials || credentials === undefined;
      const request = this.http.get<T>(url, {observe: 'response', withCredentials: withCredentials});
      this.completeRequest(request, action);
  }

  httpHead(url: string, action: (boolean) => void) {
      const request = this.http.head(url, {observe: 'response', withCredentials: true});
      this.completeRequest(request, action, undefined, true);
  }

  httpPost(url: string, body, action: () => void, errorMessage?: string) {
      const request = this.http.post(url, body, {observe: 'response', withCredentials: true});
      this.completeRequest(request, action, errorMessage);
  }

  httpDelete(url: string, action: () => void) {
      const request = this.http.delete(url, {observe: 'response', withCredentials: true});
      this.completeRequest(request, action);
  }

  httpPostThenRedirect(url: string, body) {
      this.httpPost(url, body, () => this.router.navigate(['/']));
  }

  private completeRequest<T>(request: Observable<HttpResponse<T>>, action: (T) => void, errorMessage?: string,
                             isHeadRequest?: boolean) {
      request.subscribe( response =>
              isHeadRequest !== undefined && isHeadRequest === true ? action(response.status === 200) : action(response.body),
              error => {
          if (isHeadRequest === undefined || !isHeadRequest) {
              Toast.toast(errorMessage === undefined ? 'The request could not be completed, try again' : errorMessage);
              console.error(error);
          }
      });
  }

  getReviews(urlPart: string, reviewsToShow: Review[]) {
      this.httpGetOfType<ReviewWithTimestamp[]>(Constants.restServerHost + '/' + urlPart,
          res => this.setReviews(res, reviewsToShow), 'reviews')
  }

  /**
  * If no userId is provided, the userId of the current user is used
  * */
  getReviewsByUser(urlPart: string, reviewsToShow: Review[], onCompletedAction: () => void, userId?: number) {
      let userUrlPart;
      if (userId === undefined) {
          userUrlPart = '/self/'
      } else {
          userUrlPart = '/user/' + userId + '/'
      }
      this.httpGet<ReviewWithTimestamp[]>(Constants.restServerHost + userUrlPart + urlPart + '_reviews',
          res => {
            this.setReviews(res, reviewsToShow);
            onCompletedAction();
          })
  }

  private httpGetOfType<T>(url: string, action: (T) => void, additionalUrlPart?: string) {
    this.httpGet<T>(url + '/' + this.getLastUrlPart() + '/' + this.orEmpty(additionalUrlPart),
        (res: T) => action(res))
  }

  private orEmpty(entity) {
    return entity || '';
  }

  private setReviews(reviews: ReviewWithTimestamp[], reviewsToShow: Review[]) {
      Array.prototype.forEach.call(reviews, review => {
          let itemId;
          if (review.serviceId !== undefined) {
              itemId = review.serviceId;
          } else {
              itemId = review.placeId;
          }
          reviewsToShow.push({itemId: itemId, userName: review.userName, userId: review.userId,
          stars: review.stars, comment: review.comment, date: new Date(review.date * 1000)})
      })
  }

  deleteReview(reviews: Review[], action: () => void) {
      const itemId = Number.parseInt(this.getUrlPart(3));
      const itemType = (this.getUrlPart(2).includes('service')) ? 'service' : 'place';
      this.httpDelete(Constants.restServerHost + '/private/' + itemType + '/' + itemId + '/review', () => {
          reviews.length = 0;
          action();
      })
  }

  getService(action: (Object) => void) {
      this.httpGet(Constants.restServerHost + '/service/' + this.getUrlPart(3), res => action(res))
  }

  getUserName(userId: number, action: (string) => void) {
      this.httpGetOfType<UserIdItem>(Constants.restServerHost + '/user/' + userId + '/user_name',
              res => {
          action(res.userName);
          console.log(res.userName)
              })
  }
}
