import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-suggestion',
  templateUrl: './suggestion.component.html',
  styleUrls: ['./suggestion.component.scss']
})
export class SuggestionComponent implements OnInit {

  votesCount = 0;
  gameName = 'game';
  gameDescription = 'description';

  constructor() { }

  ngOnInit() {
  }

}
