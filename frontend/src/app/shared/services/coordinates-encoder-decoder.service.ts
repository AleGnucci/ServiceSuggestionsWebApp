import { Injectable } from '@angular/core';
import {HttpUtilitiesService} from './http-utilities.service';
import {LatLng} from '@agm/core';
import {Constants} from '../constants/Constants';

@Injectable({
  providedIn: 'root'
})
export class CoordinatesEncoderDecoderService {

  private baseUrl = 'https://nominatim.openstreetmap.org/';

  /*
  private static getSpecialPhraseForServiceType(serviceType: string): string { // TODO
      return serviceType
  }
  */

  private static getOverpassNodeType(serviceType: string): [string, string] {
      const amenity = 'amenity';
      switch (serviceType) {
          case Constants.serviceCategories[0]: { // Art
              return [amenity, 'arts_centre']
          }
          case Constants.serviceCategories[1]: { // Clothes
              return [amenity, 'arts_centre']
          }
      }
  }

  constructor(private httpUtilities: HttpUtilitiesService) { }

  getEncodedResults(description: string, resultHandler: (res: NominatimLocation[]) => void, errorHandler?: () => void) {
      const descriptionWithoutSpaces = this.removeSpaces(description);
      this.httpUtilities.httpGet(this.baseUrl + '?format=json&q=' + descriptionWithoutSpaces,
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

  getEncodedResultsByType(serviceCategory: string, place: string,
                          resultHandler: (res: NominatimLocation[]) => void, errorHandler?: () => void) {
      const overpassUrl = 'http://overpass-api.de/api/interpreter?data=[out:json];';
      const placeWithoutSpaces = this.removeSpaces(place);
      /*
      const specialPhrase = CoordinatesEncoderDecoderService.getSpecialPhraseForServiceType(serviceCategory);
      const queryUrl = specialPhrase + ' in ' + place;
      this.getEncodedResults(queryUrl, resultHandler, errorHandler)
      */
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
