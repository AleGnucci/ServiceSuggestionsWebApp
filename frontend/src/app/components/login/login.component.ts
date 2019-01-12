import { Component, OnInit } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Constants} from '../../shared/constants/Constants';
import {Toast} from '../../shared/utilities/Toast';

@Component({
  selector: 'app-login',
  templateUrl: '../../shared/template/login-register.html',
  styleUrls: ['../../shared/css/login-register.scss']
})
export class LoginComponent implements OnInit {

    data: Date = new Date();
    focus;
    pageName = 'Login';

    constructor(private http: HttpClient, private router: Router, private httpUtilities: HttpUtilitiesService) {
    }

    ngOnInit() {
    }

    buttonClick(user: string, password: string) {
        const body = {
            'password' : password
        };
        const checkResult = Toast.checkNonEmptyStrings(user, password);
        if (!checkResult) {
            Toast.toast('Missing credentials, try again');
            return;
        }
        this.httpUtilities.httpPost(Constants.restServerHost + '/user/' + user + '/session', body,
            () => {
            Toast.toast('Logged in successfully');
                this.router.navigate(['/']);
                localStorage.setItem('userName', user);
            }, 'Wrong credentials, try again');
    }
}
