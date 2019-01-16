import { Injectable } from '@angular/core';
import {HttpUtilitiesService} from './http-utilities.service';
import {LatLng} from '@agm/core';
import {Constants} from '../constants/Constants';

@Injectable({
  providedIn: 'root'
})
export class CoordinatesEncoderDecoderService {

  private baseUrl = 'https://nominatim.openstreetmap.org/';

  constructor(private httpUtilities: HttpUtilitiesService) { }

  getEncodedResults(description: string, resultHandler: (res: NominatimLocation[]) => void, errorHandler?: () => void) {
      const descriptionWithoutSpaces = this.removeSpaces(description);
      const viewBox = 'viewbox=6.7499552751%2C36.619987291%2C18.4802470232%2C47.1153931748&bounded=1'; //italy bounding box
      this.httpUtilities.httpGet(this.baseUrl + '?format=json&q=' + descriptionWithoutSpaces + '&' + viewBox,
          res => {
          if (res[0] === undefined) {
              if (errorHandler !== undefined) {
                  errorHandler()
              }
          } else {
              resultHandler(res.filter(place => place.osm_type === 'node'))
          }}, false)
  }

  private removeSpaces(text: string): string {
      return text.split(' ').join('+')
  }

  getNodeById(nodeId: number, resultHandler: (res: NominatimLocation) => void) {
      this.httpUtilities.httpGet(this.baseUrl + 'reverse?format=json&osm_type=N&osm_id=' + nodeId,
              res => resultHandler(res), false);
  }

  decode(latitude: Number, longitude: Number, resultHandler: (String) => void) {
      this.httpUtilities.httpGet(this.baseUrl + 'reverse?format=json&lat=' +
          latitude + '&lon=' + longitude + '&zoom=18&addressdetails=1',
              res => resultHandler(res.display_name), false)
  }
}
