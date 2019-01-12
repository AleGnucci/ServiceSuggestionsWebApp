import {MapHelper} from './MapHelper';
import {Select2Helper} from './Select2Helper';

export abstract class UnusedCode {

    placeSelected = false;
    placeLocations = [];

    private init(){
        Select2Helper.convertToSelect2('placeSelect', 'Select a place', e => {
            this.centerMapWithEvent(e)
        });
    }

    private centerMapWithEvent(event: Event) {
        const location = this.placeLocations[event.target['selectedIndex'] - 1];
        this.placeSelected = true;
        MapHelper.showIFrame(location);
    }
}
