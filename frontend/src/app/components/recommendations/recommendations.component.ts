import { Component, OnInit } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Constants} from '../../shared/constants/Constants';

@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['../../shared/css/card-list.scss']
})

export class RecommendationsComponent implements OnInit {

  recommendations: Service[] = [];
  searchCompleted = false;

  constructor(private http: HttpClient, private router: Router, private httpUtilities: HttpUtilitiesService) { }

  ngOnInit() {
      this.httpUtilities.httpGet(Constants.restServerHost + '/private/recommendations/service_category/' +
          this.httpUtilities.getUrlPart(3) + '/get', res => {
          this.setRecommendations(res.recommendations);
          this.searchCompleted = true;
      })
  }

  setRecommendations(serviceIds: Number[]) {
      serviceIds.forEach(serviceId => {
          this.httpUtilities.httpGet<Service>(Constants.restServerHost + '/service/' + serviceId,
              res => this.setRecommendation(res))
    })
  }

  setRecommendation(service: Service) {
    this.recommendations.push(service)
  }

}
