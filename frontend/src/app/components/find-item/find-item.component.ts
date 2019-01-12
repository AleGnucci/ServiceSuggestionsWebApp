import { Component, OnInit } from '@angular/core';
import {StringUtilities} from '../../shared/utilities/StringUtilities';
import {HttpUtilitiesService} from '../../shared/services/http-utilities.service';
import {Constants} from '../../shared/constants/Constants';
import {CoordinatesEncoderDecoderService} from '../../shared/services/coordinates-encoder-decoder.service';
import {Router} from '@angular/router';
import {Select2Helper} from '../../shared/utilities/Select2Helper';
import {Toast} from '../../shared/utilities/Toast';

@Component({
  selector: 'app-find-item',
  templateUrl: './find-item.component.html',
  styleUrls: ['../../shared/css/item-form.scss']
})
export class FindItemComponent implements OnInit {

  itemType = StringUtilities.getItemType(this.httpUtilities);
  isService = this.itemType === 'Service';
  items = [];
  placeIds = [];
  searchCompleted = false;
  // serviceCategories = Constants.serviceCategories;

  constructor(private httpUtilities: HttpUtilitiesService, private coordsEncoderDecoder: CoordinatesEncoderDecoderService,
              private router: Router) { }

  ngOnInit() {
  }

  findService(inputId: string) {
    const input = <HTMLInputElement>document.getElementById(inputId);
    this.checkNonEmptyString(input.value, serviceName => {
        this.httpUtilities.httpGet(Constants.restServerHost +
            '/services/with_similar_name/' + serviceName, res => this.resultHandler(res[0]))
    });
}

  /*
  findService(inputId: string, selectId: string) {
      const placeInput = <HTMLInputElement>document.getElementById(inputId);
      const select = <HTMLSelectElement>document.getElementById(selectId);
      this.checkNonEmptyString(placeInput.value, servicePlace => {
          this.checkNonEmptyString(select.value, serviceCategory => {
              this.coordsEncoderDecoder.getEncodedResultsByType(serviceCategory, servicePlace,
              res => this.resultHandler(res.map(service => this.convertLocationToServiceItem(service, serviceCategory))),
                  () => console.error('error in findService'))
              // this.httpUtilities.httpGet(Constants.restServerHost + '/services/with_similar_name/' + serviceName, res => this.resultHandler(res[0]))
          })
      });
  }

  private convertLocationToServiceItem(location: NominatimLocation, serviceCategory: string): ServiceItem {
      return <ServiceItem>{id: location.osm_id, category: serviceCategory, name: location.display_name, placeId: location.osm_id}
  }
  */

  private checkNonEmptyString(string: string, continuation: (string) => void) {
      if (string.length === 0) {
          Toast.toast('Empty search field, try again')
      } else {
          continuation(string);
      }
  }

  private resultHandler(result: Object[]) {
      this.items = this.getCompleteArray(result);
      this.searchCompleted = true;
  }

  getCompleteArray(array: Object[]): Object[] {
      return array.map(res => {
          const result = this.isService ? (res as ServiceItem) : (res as PlaceItem);
          if (this.isService) {
              (result as ServiceItem).name = StringUtilities.capitalizeFirstLetter((result as ServiceItem).name);
          }
          if (!this.isService) {
              const location = (result as PlaceItem).location;
              delete (result as PlaceItem).location;
              this.coordsEncoderDecoder.decode(location[0], location[1],
                      description => (result as PlaceItem).location = description)
          }
          return result;
      })
  }

  findPlace(nameId: string) {
      const nameInput = <HTMLInputElement>document.getElementById(nameId);
      this.checkNonEmptyString(nameInput.value, placeName => {
          this.coordsEncoderDecoder.getEncodedResults(placeName,
              res => this.resultHandler(res.map(place =>
                  <PlaceItem>{id: place.osm_id, location: [place.lat, place.lon]})))
      });
  }

  goToReviews(itemType: string, itemId: number) {
      this.router.navigate(['/reviews/' + itemType.toLowerCase() + '/' + itemId]);
  }
}
