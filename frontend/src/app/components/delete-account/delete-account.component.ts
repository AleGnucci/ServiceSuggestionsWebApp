import { Component, OnInit } from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Constants} from '../../shared/constants/Constants';
import {Toast} from '../../shared/utilities/Toast';
import {CookieManager} from '../../shared/utilities/CookieManager';

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
        () => {
          localStorage.removeItem('userName');
          CookieManager.deleteCookie(CookieManager.SESSION_COOKIE_NAME);
          setTimeout(() => location.href = '/', 0);
          Toast.toast('Account deleted successfully')
        })
  }
}
