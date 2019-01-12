declare var jQuery: any;

export abstract class Select2Helper {
    static convertToSelect2(selectId: string, placeHolderText: string, action: (Event) => void) {
        this.setupPlaceSelectStyle(selectId, placeHolderText);
        this.setupPlaceSelectBehaviour(selectId, action);
    }

    private static setupPlaceSelectStyle(selectId: string, placeHolderText: string) {
        jQuery( document ).ready(function() {
            jQuery('#' + selectId).select2({
                placeholder: placeHolderText,
                allowClear: true,
                width: '100%'
            });
            jQuery('.select2-container--default .select2-selection--single').css({
                'background-color': '#808080',
                'border-color': '#808080',
                'border-radius': '50px',
                'height': '45px',
                'margin': 'auto'});
            jQuery('.select2-selection__rendered').css({
                'color': '#DDD',
                'margin': '10px',
                'text-align': 'left'});
            jQuery('.select2-selection__placeholder').css({
                'float': 'left',
                'color': '#DDD',
                'font-size': '12px',
                'margin-top': '-2px'
            });
            jQuery('.select2-selection__arrow').css({'margin-top': '8px'});
            jQuery('b[role="presentation"]').css({'color': 'white'});
            jQuery('.select2').css({'margin-top': '20px'})
        });
    }

    private static setupPlaceSelectBehaviour(selectId: string, action: (Event) => void) {
        jQuery('#' + selectId).on('select2:select', e => {
            jQuery('.select2-selection__clear').addClass('invisible');
            action(e);
        });
    }
}