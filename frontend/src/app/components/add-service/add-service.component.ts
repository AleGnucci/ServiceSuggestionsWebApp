import {AfterViewInit, Component} from '@angular/core';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Constants} from '../../shared/constants/Constants';
import {StringUtilities} from '../../shared/utilities/StringUtilities';
import {CoordinatesEncoderDecoderService} from '../../shared/services/coordinates-encoder-decoder.service';
import {Toast} from '../../shared/utilities/Toast';
import {MapHelper} from '../../shared/utilities/MapHelper';

@Component({
  selector: 'app-add-item',
  templateUrl: './add-service.component.html',
  styleUrls: ['../../shared/css/item-form.scss']
})

export class AddServiceComponent implements AfterViewInit {

  itemType = StringUtilities.getItemType(this.httpUtilities);
  serviceCategories = Constants.serviceCategories;
  places = [];
  placeSelected = false;

  constructor(private httpUtilities: HttpUtilitiesService, private coordEncoderDecoder: CoordinatesEncoderDecoderService) { }

  ngAfterViewInit() {
      this.initSelectablePlaces();
  }

  private initSelectablePlaces() {
  }

  addService(categorySelectId: string, nameInputId: string, latLngInput: string, textareaId: string) {
    const category = <HTMLSelectElement>document.getElementById(categorySelectId);
    const name = <HTMLInputElement>document.getElementById(nameInputId);
    const placeInput = <HTMLInputElement>document.getElementById(latLngInput);
    const textarea = <HTMLTextAreaElement>document.getElementById(textareaId);
    const body = {category: category.value, name: name.value,
      placeId: Number.parseInt(placeInput.value), description: textarea.value};
    if (Toast.checkNonEmptyFields(category, name, placeInput, textarea)) {
      this.httpUtilities.httpPostThenRedirect(Constants.restServerHost + '/private/service', body);
      Toast.toast('Service added successfully');
    }
  }

  getPlaces(locationDescriptionInputId: string) {
    const locationDescriptionInput = <HTMLInputElement>document.getElementById(locationDescriptionInputId);
      const description = locationDescriptionInput.value.replace(/ /g, '+');
      this.coordEncoderDecoder.getEncodedResults(description, results => {
          this.places = results.slice(0, 3);
          if(this.places.length === 0){
            Toast.toast('No places found');
          }
      }, () => Toast.toast('The inserted location does not exist'));
  }

  choosePlace(lat: number, lon: number, placeId: number, locationInputId: string) {
    const latLngInput = <HTMLInputElement>document.getElementById(locationInputId);
      latLngInput.value = placeId.toString(10);
      this.placeSelected = true;
      MapHelper.showIFrame([lat, lon]);
  }
}
