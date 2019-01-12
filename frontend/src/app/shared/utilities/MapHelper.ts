declare var jQuery: any;

export abstract class MapHelper {

    static showIFrame(location: number[]): void {
        jQuery('#iframe-div').css({'display': 'block'});
        setTimeout(() => {
            const iFrame = <HTMLIFrameElement>document.getElementById('iframe-id');
            const zoomLevel = 0.005;
            iFrame.src = 'https://www.openstreetmap.org/export/embed.html?' +
                'bbox=' + (location[1] - zoomLevel) + '%2C' + (location[0] - zoomLevel) + '%2C' +
                (Number(location[1]) + +zoomLevel) + '%2C' + (Number(location[0]) + +zoomLevel) +
                '&amp;layer=mapnik&marker=' + location[0] + '%2C' + location[1];
        }, 0);
    }
}
