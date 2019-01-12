import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {CoordinatesEncoderDecoderService} from '../../shared/services/coordinates-encoder-decoder.service';
import {Constants} from '../../shared/constants/Constants';
import {Toast} from '../../shared/utilities/Toast';

@Component({
  selector: 'app-service-info',
  templateUrl: './service-info.component.html',
  styleUrls: ['./service-info.component.scss']
})
export class ServiceInfoComponent implements OnInit {

  service: ServiceItem = {id: 0, category: '', placeId: 0, name: '', description: ''};
  placeDescription = '';
  showVoteButtons = true;

  constructor(private httpUtilities: HttpUtilitiesService,
              private coordEncoderDecoder: CoordinatesEncoderDecoderService) { }

  ngOnInit() {
      this.httpUtilities.getService(res => {
          this.service = res.item;
          this.coordEncoderDecoder.getNodeById(res.item.placeId, place => {
              this.placeDescription = place.display_name;
          });
      });
      this.showOrHideVoteButtons();
  }

  private showOrHideVoteButtons() {
      this.httpUtilities.httpHead(Constants.restServerHost + '/private/service/' + this.httpUtilities.getUrlPart(3) + '/vote',
          voteExists => this.showVoteButtons = !voteExists)
  }

  voteInfo(vote: Boolean) {
    let voteString;
    if (vote) {
      voteString = '/voteForCorrectData';
    }  else {
      voteString = '/voteForWrongData';
    }
    this.httpUtilities.httpPost(Constants.restServerHost + '/private/service/' + this.service.id +
        voteString, null, () => {
        Toast.toast('Vote completed');
        this.showOrHideVoteButtons();
    });
  }
}
