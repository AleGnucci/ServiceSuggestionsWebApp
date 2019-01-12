import { Component, OnInit, Renderer, OnDestroy } from '@angular/core';
import { NgbAccordionConfig } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-components',
    templateUrl: './components.component.html',
    styles: [`
    ngb-progressbar {
        margin-top: 5rem;
    }
    h2{
       font-size: x-large;
    }
    div{
        overflow-x: hidden;
    }
    @media screen and (max-width: 768px) {
        h1{
            font-size: x-large !important;
        }
        h2{
            font-size: larger;
        }
    }
    `]
})

export class ComponentsComponent implements OnInit, OnDestroy {
    data: Date = new Date();
    page = 4;
    focus;
    date: {year: number, month: number};

    constructor( private renderer: Renderer, config: NgbAccordionConfig) {
        config.closeOthers = true;
        config.type = 'info';
    }

    ngOnInit() {
        const body = document.getElementsByTagName('body')[0];
        body.classList.add('index-page');
    }

    ngOnDestroy() {
        const navbar = document.getElementsByTagName('nav')[0];
        navbar.classList.remove('navbar-transparent');
        const body = document.getElementsByTagName('body')[0];
        body.classList.remove('index-page');
    }
}
