import { Component, OnInit } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {Constants} from '../../shared/constants/Constants';
import {Toast} from '../../shared/utilities/Toast';

declare var zxcvbn: any;

@Component({
  selector: 'app-register',
  templateUrl: '../../shared/template/login-register.html',
  styleUrls: ['../../shared/css/login-register.scss']
})
export class RegisterComponent implements OnInit {

  pageName = 'Register';

  constructor(private http: HttpClient, private router: Router) { }

  ngOnInit() {}

  buttonClick(user: string, password: string) {
      const body = {
          'password' : password,
      };
      if (this.areCredentialsStrong(user, password)) {
          this.http.post<Object>(Constants.restServerHost + '/user/' + user, body, {observe: 'response'})
              .subscribe( (response: any) => {
                  if (parseInt((response.status / 100).toString(), 10) === 2) {
                      Toast.toast('User registration complete');
                      this.router.navigate(['/'])
                  }
              }, _ => Toast.toast('User already exists, try again'));
      } else {
          Toast.toast('The provided credentials are not strong enough, try again');
      }
  }

  private areCredentialsStrong(user: string, password: string): boolean {
      return user.length > 0 && password.length >= 10 && zxcvbn(password).score >= 3;
  }

}
