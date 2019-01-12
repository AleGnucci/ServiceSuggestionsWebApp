import {HttpUtilitiesService} from '../services/http-utilities.service';

export abstract class StringUtilities {

    static getItemType(httpUtilities: HttpUtilitiesService): string {
        return StringUtilities.capitalizeFirstLetter(httpUtilities.getUrlPart(1));
    }

    static capitalizeFirstLetter(string): string {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }
}
