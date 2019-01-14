interface ReviewWithTimestamp {
    placeId: Number;
    serviceId: Number;
    userId: Number;
    userName: string;
    stars: Number;
    comment: string;
    date: number;
}

interface Review {
    itemId: Number;
    userName: string;
    userId: Number;
    stars: Number;
    comment: string;
    date: Date;
}

interface Place {
    item: PlaceItem;
    stars: any;
}

interface PlaceItem {
    id: Number;
    location: Number[];
}

interface NominatimLocation {
    osm_id: number;
    display_name: string;
    importance: number;
    lat: number;
    lon: number;
}

interface PlaceItemWithDescription {
    id: Number;
    description: string;
    location: number[];
}

interface Service {
    item: ServiceItem;
    stars: any;
}

interface ServiceItem {
    id: Number;
    category: string;
    name: string;
    placeId: Number;
    description: string;
    startDateTime?: string;
    endDateTime?: string;
}

interface UserIdItem {
    userName: string;
    id: number;
}