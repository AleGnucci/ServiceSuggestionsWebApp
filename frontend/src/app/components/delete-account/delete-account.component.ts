import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Constants} from '../../shared/constants/Constants';
import {Toast} from '../../shared/utilities/Toast';

@Component({
  selector: 'app-delete-account',
  templateUrl: './delete-account.component.html',
  styleUrls: ['./delete-account.component.scss']
})
export class DeleteAccountComponent implements OnInit {

  constructor(private httpUtilities: HttpUtilitiesService) { }

  ngOnInit() {
  }

  deleteAccount(){
    this.httpUtilities.httpDelete(Constants.restServerHost + '/private/user',
        () => Toast.toast('Account deleted successfully'))
  }
}
