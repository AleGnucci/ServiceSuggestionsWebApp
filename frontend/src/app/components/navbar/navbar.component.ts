///<reference path="../../shared/constants/Constants.ts"/>
import {Component, OnInit, ElementRef, ChangeDetectorRef} from '@angular/core';
import { Location} from '@angular/common';
import {Constants} from '../../shared/constants/Constants';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Router} from '@angular/router';
import {Toast} from '../../shared/utilities/Toast';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})

export class NavbarComponent implements OnInit {
    private toggleButton: any;
    private SESSION_COOKIE_NAME = 'vertx-web.session';
    serviceCategories = Constants.serviceCategories;

    private static getCookie(name: string): string {
        const cookies: Array<string> = document.cookie.split(';');
        const cookiesCount: number = cookies.length;
        const cookieName = `${name}=`;
        let c: string;

        for (let i = 0; i < cookiesCount; i += 1) {
            c = cookies[i].replace(/^\s+/g, '');
            if (c.indexOf(cookieName) === 0) {
                return c.substring(cookieName.length, c.length);
            }
        }
        return '';
    }

    private static deleteCookie(name) {
        NavbarComponent.setCookie(name, '', -1);
    }

    private static setCookie(name: string, value: string, expireDays: number, path: string = '') {
        const date = new Date();
        date.setTime(date.getTime() + expireDays * 24 * 60 * 60 * 1000);
        const expires = `expires=${date.toUTCString()}` + ';path=/';
        const cpath = path ? `; path=${path}` : '';
        document.cookie = `${name}=${value}; ${expires}${cpath}`;
    }

    constructor(public location: Location, private element: ElementRef, private httpUtilities: HttpUtilitiesService,
                public router: Router) {}

    ngOnInit() {
        const navbar: HTMLElement = this.element.nativeElement;
        this.toggleButton = navbar.getElementsByClassName('navbar-toggler')[0];
    }

    sidebarOpen() {
        const toggleButton = this.toggleButton;
        const html = document.getElementsByTagName('html')[0];
        setTimeout(function(){
            toggleButton.classList.add('toggled');
        }, 500);
        html.classList.add('nav-open');
    }

    sidebarClose() {
        const html = document.getElementsByTagName('html')[0];
        this.toggleButton.classList.remove('toggled');
        html.classList.remove('nav-open');
    }

    logout() {
        this.httpUtilities.httpDelete(Constants.restServerHost + '/user/session',
            () => {
            localStorage.removeItem('userName');
            NavbarComponent.deleteCookie(this.SESSION_COOKIE_NAME);
            setTimeout(() => location.href = '/', 0);
            Toast.toast('Logged out successfully')
        });
    }

    isLoggedIn() {
        return localStorage.getItem('userName') !== null &&
            NavbarComponent.getCookie(this.SESSION_COOKIE_NAME) !== '';
    }

    openDropdownMenu(dropdownMenuIndex: number) {
        const button = <HTMLButtonElement>document.getElementsByClassName('dropdown-toggle').item(dropdownMenuIndex);
        const dropdownMenu = <HTMLUListElement>document.getElementsByClassName('dropdown-menu').item(dropdownMenuIndex);
        if (!this.isVisible(dropdownMenu)) { // dropdown is not visible
            button.click();
            (<HTMLElement>dropdownMenu.children[0]).focus();
            window.setTimeout(() => (<HTMLElement>dropdownMenu.children[0]).focus(), 0);
        }
    }

    private isVisible (element: HTMLElement) {
        const style = window.getComputedStyle(element);
        return  style.width !== '0' &&
            style.height !== '0' &&
            style.opacity !== '0' &&
            style.display !== 'none' &&
            style.visibility !== 'hidden';
    }
}
