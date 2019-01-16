export abstract class DateChecker {

    static checkIfServiceReviewable(service: ServiceItem): boolean {
        return service.startDateTime === undefined || new Date(service.startDateTime) < new Date()
    }

}